"use client";

import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import {
  TrendingUp,
  TrendingDown,
  Wallet,
  Users,
  Plus,
  ArrowUpRight,
  ArrowDownRight,
  Receipt,
  PiggyBank,
} from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import Link from "next/link";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { personalService } from "@/services/personal-service";
import { groupService } from "@/services/group-service";
import { formatCurrency, getCurrentMonth } from "@/lib/utils";

const CHART_COLORS = ["#6366f1", "#8b5cf6", "#a78bfa", "#c4b5fd", "#10b981", "#f59e0b", "#f43f5e", "#06b6d4"];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.1 },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0 },
};

export default function DashboardPage() {
  const currentMonth = getCurrentMonth();

  const { data: summary } = useQuery({
    queryKey: ["monthly-summary", currentMonth],
    queryFn: () => personalService.getMonthlySummary(currentMonth),
  });

  const { data: groups } = useQuery({
    queryKey: ["groups"],
    queryFn: () => groupService.getGroups(),
  });

  const { data: transactions } = useQuery({
    queryKey: ["transactions-recent"],
    queryFn: () => personalService.getTransactions(),
  });

  const { data: categories } = useQuery({
    queryKey: ["categories"],
    queryFn: () => personalService.getCategories(),
  });

  // Build category spending data for pie chart
  const categorySpending = transactions
    ?.filter((t) => t.type === "EXPENSE")
    .reduce((acc, t) => {
      const cat = t.categoryName || "Other";
      acc[cat] = (acc[cat] || 0) + t.amount;
      return acc;
    }, {} as Record<string, number>);

  const pieData = categorySpending
    ? Object.entries(categorySpending)
        .map(([name, value]) => ({ name, value }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 8)
    : [];

  // Recent transactions for activity feed
  const recentTransactions = transactions
    ?.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 6);

  const summaryCards = [
    {
      title: "Total Income",
      value: formatCurrency(summary?.totalIncome || 0),
      icon: TrendingUp,
      color: "text-emerald-500",
      bgColor: "bg-emerald-500/10",
      borderColor: "border-emerald-500/20",
    },
    {
      title: "Total Expenses",
      value: formatCurrency(summary?.totalExpenses || 0),
      icon: TrendingDown,
      color: "text-rose-500",
      bgColor: "bg-rose-500/10",
      borderColor: "border-rose-500/20",
    },
    {
      title: "Net Savings",
      value: formatCurrency(summary?.netSavings || 0),
      icon: PiggyBank,
      color: (summary?.netSavings || 0) >= 0 ? "text-emerald-500" : "text-rose-500",
      bgColor: (summary?.netSavings || 0) >= 0 ? "bg-emerald-500/10" : "bg-rose-500/10",
      borderColor: (summary?.netSavings || 0) >= 0 ? "border-emerald-500/20" : "border-rose-500/20",
    },
    {
      title: "Active Groups",
      value: groups?.length?.toString() || "0",
      icon: Users,
      color: "text-indigo-500",
      bgColor: "bg-indigo-500/10",
      borderColor: "border-indigo-500/20",
    },
  ];

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="space-y-6"
    >
      {/* Page Header */}
      <motion.div variants={itemVariants} className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <p className="text-muted-foreground mt-1">Your financial overview at a glance</p>
        </div>
        <div className="flex gap-2">
          <Link href="/dashboard/personal">
            <Button size="sm" variant="outline" className="gap-2">
              <Plus className="w-4 h-4" />
              Add Transaction
            </Button>
          </Link>
          <Link href="/dashboard/groups">
            <Button size="sm" className="gap-2">
              <Users className="w-4 h-4" />
              My Groups
            </Button>
          </Link>
        </div>
      </motion.div>

      {/* Summary Cards */}
      <motion.div variants={itemVariants} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {summaryCards.map((card) => (
          <Card
            key={card.title}
            className={`group hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border ${card.borderColor}`}
          >
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{card.title}</p>
                  <p className={`text-2xl font-bold mt-1 ${card.color}`}>{card.value}</p>
                </div>
                <div className={`w-12 h-12 rounded-xl ${card.bgColor} flex items-center justify-center`}>
                  <card.icon className={`w-6 h-6 ${card.color}`} />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </motion.div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Category */}
        <motion.div variants={itemVariants}>
          <Card className="h-full">
            <CardHeader>
              <CardTitle className="text-lg">Top Spending Category</CardTitle>
            </CardHeader>
            <CardContent>
              {summary?.topCategoryName ? (
                <div className="flex items-center gap-4">
                  <div className="w-16 h-16 rounded-2xl bg-rose-500/10 flex items-center justify-center">
                    <Receipt className="w-8 h-8 text-rose-500" />
                  </div>
                  <div>
                    <p className="text-2xl font-bold">{summary.topCategoryName}</p>
                    <p className="text-lg text-rose-500 font-semibold">
                      {formatCurrency(summary.topCategoryAmount)}
                    </p>
                    <p className="text-sm text-muted-foreground">This month</p>
                  </div>
                </div>
              ) : (
                <p className="text-muted-foreground">No spending data yet</p>
              )}
            </CardContent>
          </Card>
        </motion.div>

        {/* Category Distribution Pie */}
        <motion.div variants={itemVariants}>
          <Card className="h-full">
            <CardHeader>
              <CardTitle className="text-lg">Spending by Category</CardTitle>
            </CardHeader>
            <CardContent>
              {pieData.length > 0 ? (
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={100}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {pieData.map((_, index) => (
                          <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{
                          backgroundColor: "hsl(var(--card))",
                          border: "1px solid hsl(var(--border))",
                          borderRadius: "8px",
                          color: "hsl(var(--foreground))",
                        }}
                        formatter={(value: number) => formatCurrency(value)}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="h-64 flex items-center justify-center text-muted-foreground">
                  No expense data to display
                </div>
              )}
              {/* Legend */}
              <div className="flex flex-wrap gap-3 mt-2 justify-center">
                {pieData.map((item, i) => (
                  <div key={item.name} className="flex items-center gap-1.5 text-xs">
                    <div
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: CHART_COLORS[i % CHART_COLORS.length] }}
                    />
                    <span className="text-muted-foreground">{item.name}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>

      {/* Activity Feed */}
      <motion.div variants={itemVariants}>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg">Recent Activity</CardTitle>
            <Link href="/dashboard/personal">
              <Button variant="ghost" size="sm" className="gap-1 text-primary">
                View all <ArrowUpRight className="w-3 h-3" />
              </Button>
            </Link>
          </CardHeader>
          <CardContent>
            {recentTransactions && recentTransactions.length > 0 ? (
              <div className="space-y-3">
                {recentTransactions.map((tx) => (
                  <div
                    key={tx.id}
                    className="flex items-center justify-between p-3 rounded-lg hover:bg-accent/50 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                          tx.type === "INCOME" ? "bg-emerald-500/10" : "bg-rose-500/10"
                        }`}
                      >
                        {tx.type === "INCOME" ? (
                          <ArrowUpRight className="w-5 h-5 text-emerald-500" />
                        ) : (
                          <ArrowDownRight className="w-5 h-5 text-rose-500" />
                        )}
                      </div>
                      <div>
                        <p className="font-medium text-sm">{tx.description || tx.categoryName}</p>
                        <p className="text-xs text-muted-foreground">{tx.categoryName} · {tx.transactionDate}</p>
                      </div>
                    </div>
                    <p
                      className={`font-semibold ${
                        tx.type === "INCOME" ? "text-emerald-500" : "text-rose-500"
                      }`}
                    >
                      {tx.type === "INCOME" ? "+" : "-"}{formatCurrency(tx.amount)}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-8 text-center text-muted-foreground">
                <Receipt className="w-12 h-12 mx-auto mb-3 opacity-50" />
                <p>No transactions yet</p>
                <p className="text-sm">Start by adding your first transaction</p>
              </div>
            )}
          </CardContent>
        </Card>
      </motion.div>
    </motion.div>
  );
}
