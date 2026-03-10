"use client";

import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
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
  LineChart,
  Line,
  Legend,
} from "recharts";
import { TrendingUp, TrendingDown, PiggyBank, BarChart3, Loader2 } from "lucide-react";
import { format, subMonths } from "date-fns";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { personalService } from "@/services/personal-service";
import { formatCurrency, getCurrentMonth } from "@/lib/utils";

const CHART_COLORS = ["#6366f1", "#8b5cf6", "#a78bfa", "#c4b5fd", "#10b981", "#f59e0b", "#f43f5e", "#06b6d4"];

export default function AnalyticsPage() {
  const currentMonth = getCurrentMonth();

  const { data: summary, isLoading } = useQuery({
    queryKey: ["monthly-summary", currentMonth],
    queryFn: () => personalService.getMonthlySummary(currentMonth),
  });

  const { data: transactions } = useQuery({
    queryKey: ["transactions"],
    queryFn: () => personalService.getTransactions(),
  });

  // Monthly trend: get summaries for last 6 months
  const months = Array.from({ length: 6 }, (_, i) => {
    const d = subMonths(new Date(), 5 - i);
    return format(d, "yyyy-MM");
  });

  const trendQueries = months.map((month) =>
    // eslint-disable-next-line react-hooks/rules-of-hooks
    useQuery({
      queryKey: ["monthly-summary", month],
      queryFn: () => personalService.getMonthlySummary(month),
    })
  );

  const trendData = months.map((month, i) => ({
    month: format(new Date(month + "-01"), "MMM"),
    income: trendQueries[i]?.data?.totalIncome || 0,
    expenses: trendQueries[i]?.data?.totalExpenses || 0,
  }));

  // Category distribution
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

  const tooltipStyle = {
    contentStyle: {
      backgroundColor: "hsl(var(--card))",
      border: "1px solid hsl(var(--border))",
      borderRadius: "8px",
      color: "hsl(var(--foreground))",
    },
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6"
    >
      <div>
        <h1 className="text-3xl font-bold">Analytics</h1>
        <p className="text-muted-foreground mt-1">Insights into your spending habits</p>
      </div>

      {/* Summary Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="border-emerald-500/20">
          <CardContent className="p-6 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 flex items-center justify-center">
              <TrendingUp className="w-6 h-6 text-emerald-500" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Total Income</p>
              <p className="text-2xl font-bold text-emerald-500">
                {formatCurrency(summary?.totalIncome || 0)}
              </p>
            </div>
          </CardContent>
        </Card>

        <Card className="border-rose-500/20">
          <CardContent className="p-6 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-rose-500/10 flex items-center justify-center">
              <TrendingDown className="w-6 h-6 text-rose-500" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Total Expenses</p>
              <p className="text-2xl font-bold text-rose-500">
                {formatCurrency(summary?.totalExpenses || 0)}
              </p>
            </div>
          </CardContent>
        </Card>

        <Card className={`border-${(summary?.netSavings || 0) >= 0 ? "emerald" : "rose"}-500/20`}>
          <CardContent className="p-6 flex items-center gap-4">
            <div className={`w-12 h-12 rounded-xl ${
              (summary?.netSavings || 0) >= 0 ? "bg-emerald-500/10" : "bg-rose-500/10"
            } flex items-center justify-center`}>
              <PiggyBank className={`w-6 h-6 ${
                (summary?.netSavings || 0) >= 0 ? "text-emerald-500" : "text-rose-500"
              }`} />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Net Savings</p>
              <p className={`text-2xl font-bold ${
                (summary?.netSavings || 0) >= 0 ? "text-emerald-500" : "text-rose-500"
              }`}>
                {formatCurrency(summary?.netSavings || 0)}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Income vs Expenses Trend */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Income vs Expenses (6 months)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={trendData} barGap={8}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                  <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" fontSize={12} />
                  <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} />
                  <Tooltip {...tooltipStyle} formatter={(value: number) => formatCurrency(value)} />
                  <Legend />
                  <Bar dataKey="income" fill="#10b981" name="Income" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="expenses" fill="#f43f5e" name="Expenses" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Category Distribution */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Spending Distribution</CardTitle>
          </CardHeader>
          <CardContent>
            {pieData.length > 0 ? (
              <>
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={55}
                        outerRadius={95}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {pieData.map((_, index) => (
                          <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip {...tooltipStyle} formatter={(value: number) => formatCurrency(value)} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex flex-wrap gap-3 mt-4 justify-center">
                  {pieData.map((item, i) => (
                    <div key={item.name} className="flex items-center gap-1.5 text-xs">
                      <div
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: CHART_COLORS[i % CHART_COLORS.length] }}
                      />
                      <span className="text-muted-foreground">{item.name}</span>
                      <span className="font-medium">{formatCurrency(item.value)}</span>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className="h-64 flex items-center justify-center text-muted-foreground">
                No expense data to display
              </div>
            )}
          </CardContent>
        </Card>

        {/* Monthly Spending Trend Line */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">Monthly Spending Trend</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trendData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                  <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" fontSize={12} />
                  <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} />
                  <Tooltip {...tooltipStyle} formatter={(value: number) => formatCurrency(value)} />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="expenses"
                    stroke="#f43f5e"
                    strokeWidth={2}
                    dot={{ fill: "#f43f5e", r: 4 }}
                    name="Expenses"
                  />
                  <Line
                    type="monotone"
                    dataKey="income"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={{ fill: "#10b981", r: 4 }}
                    name="Income"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Highest Spending Category */}
      {summary?.topCategoryName && (
        <Card className="border-rose-500/20">
          <CardContent className="p-6">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-rose-500/10 flex items-center justify-center">
                <BarChart3 className="w-7 h-7 text-rose-500" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Highest Spending Category</p>
                <p className="text-xl font-bold">{summary.topCategoryName}</p>
                <p className="text-lg font-semibold text-rose-500">
                  {formatCurrency(summary.topCategoryAmount)} this month
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </motion.div>
  );
}
