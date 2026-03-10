"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { toast } from "sonner";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, Trash2, PiggyBank, Loader2 } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Progress } from "@/components/ui/progress";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { personalService } from "@/services/personal-service";
import { formatCurrency } from "@/lib/utils";
import { BudgetPeriod } from "@/types";

const budgetSchema = z.object({
  categoryId: z.string().optional(),
  limitAmount: z.coerce.number().positive("Amount must be positive"),
  period: z.nativeEnum(BudgetPeriod),
});

type BudgetFormData = z.infer<typeof budgetSchema>;

export default function BudgetsPage() {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [isAddingCategory, setIsAddingCategory] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState("");

  const { data: budgets, isLoading } = useQuery({
    queryKey: ["budgets"],
    queryFn: () => personalService.getBudgets(),
  });

  const { data: categories } = useQuery({
    queryKey: ["categories"],
    queryFn: () => personalService.getCategories(),
  });

  const createMutation = useMutation({
    mutationFn: personalService.createBudget,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["budgets"] });
      toast.success("Budget created");
      setDialogOpen(false);
    },
    onError: (err: any) => {
      toast.error("Failed to create budget", {
        description: err.response?.data?.message || "Please try again",
      });
    },
  });

  const createCategoryMutation = useMutation({
    mutationFn: personalService.createCategory,
    onSuccess: (newCat) => {
      queryClient.invalidateQueries({ queryKey: ["categories"] });
      toast.success("Category created");
      setValue("categoryId", newCat.id);
      setIsAddingCategory(false);
      setNewCategoryName("");
    },
    onError: () => toast.error("Failed to create category"),
  });

  const deleteMutation = useMutation({
    mutationFn: personalService.deleteBudget,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["budgets"] });
      toast.success("Budget deleted");
    },
    onError: () => toast.error("Failed to delete budget"),
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<BudgetFormData>({
    resolver: zodResolver(budgetSchema),
    defaultValues: { period: BudgetPeriod.MONTHLY },
  });

  const expenseCategories = categories?.filter((c) => c.type === "EXPENSE");

  const onSubmit = (data: BudgetFormData) => {
    createMutation.mutate({
      categoryId: data.categoryId || null,
      limitAmount: data.limitAmount,
      period: data.period,
    });
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6"
    >
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Budgets</h1>
          <p className="text-muted-foreground mt-1">Track your spending limits</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={(open) => { setDialogOpen(open); if (open) reset(); }}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="w-4 h-4" />
              Create Budget
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create Budget</DialogTitle>
              <DialogDescription>Set a spending limit for a category</DialogDescription>
            </DialogHeader>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label>Category (optional — leave blank for overall)</Label>
                  <Button
                    type="button"
                    variant="link"
                    size="sm"
                    className="h-auto p-0 text-xs"
                    onClick={() => setIsAddingCategory((prev) => !prev)}
                  >
                    {isAddingCategory ? "Cancel" : "+ New Category"}
                  </Button>
                </div>
                
                {isAddingCategory ? (
                  <div className="flex items-center gap-2">
                    <Input
                      placeholder="New expense category"
                      value={newCategoryName}
                      onChange={(e) => setNewCategoryName(e.target.value)}
                      className="h-9"
                    />
                    <Button
                      type="button"
                      size="sm"
                      className="h-9"
                      disabled={!newCategoryName.trim() || createCategoryMutation.isPending}
                      onClick={() => {
                        createCategoryMutation.mutate({
                          name: newCategoryName.trim(),
                          type: "EXPENSE" as any,
                        });
                      }}
                    >
                      {createCategoryMutation.isPending ? <Loader2 className="w-4 h-4 animate-spin" /> : "Save"}
                    </Button>
                  </div>
                ) : (
                  <Select
                    value={watch("categoryId") || ""}
                    onValueChange={(val) => setValue("categoryId", val)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Overall budget" />
                    </SelectTrigger>
                    <SelectContent>
                      {expenseCategories?.length ? (
                        expenseCategories.map((cat) => (
                          <SelectItem key={cat.id} value={cat.id}>
                            {cat.name}
                          </SelectItem>
                        ))
                      ) : (
                        <div className="p-2 text-sm text-muted-foreground text-center">
                          No expense categories - create one!
                        </div>
                      )}
                    </SelectContent>
                  </Select>
                )}
              </div>

              <div className="space-y-2">
                <Label>Limit Amount</Label>
                <Input type="number" step="0.01" placeholder="5000" {...register("limitAmount")} />
                {errors.limitAmount && (
                  <p className="text-sm text-destructive">{errors.limitAmount.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label>Period</Label>
                <Select
                  value={watch("period")}
                  onValueChange={(val) => setValue("period", val as BudgetPeriod)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="WEEKLY">Weekly</SelectItem>
                    <SelectItem value="MONTHLY">Monthly</SelectItem>
                    <SelectItem value="YEARLY">Yearly</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <DialogFooter>
                <Button type="submit" disabled={createMutation.isPending} className="w-full">
                  {createMutation.isPending ? (
                    <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Creating...</>
                  ) : (
                    "Create Budget"
                  )}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {/* Budgets Grid */}
      {isLoading ? (
        <div className="flex items-center justify-center py-16">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      ) : budgets && budgets.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {budgets.map((budget) => {
            const percentage = budget.limitAmount > 0
              ? Math.min(100, (budget.spentAmount / budget.limitAmount) * 100)
              : 0;
            const isOverBudget = budget.spentAmount > budget.limitAmount;
            const isWarning = percentage >= 80 && !isOverBudget;

            return (
              <motion.div
                key={budget.id}
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
              >
                <Card className={`group hover:shadow-xl transition-all duration-300 hover:-translate-y-1 ${
                  isOverBudget ? "border-rose-500/30" : isWarning ? "border-amber-500/30" : ""
                }`}>
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <p className="font-semibold text-lg">
                          {budget.categoryName || "Overall Budget"}
                        </p>
                        <span className="text-xs bg-accent px-2 py-0.5 rounded-md capitalize">
                          {budget.period.toLowerCase()}
                        </span>
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground hover:text-destructive"
                        onClick={() => deleteMutation.mutate(budget.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>

                    <div className="space-y-3">
                      <Progress
                        value={percentage}
                        indicatorClassName={
                          isOverBudget
                            ? "bg-rose-500"
                            : isWarning
                            ? "bg-amber-500"
                            : "bg-emerald-500"
                        }
                      />

                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          Spent: <span className={`font-semibold ${
                            isOverBudget ? "text-rose-500" : "text-foreground"
                          }`}>{formatCurrency(budget.spentAmount)}</span>
                        </span>
                        <span className="text-muted-foreground">
                          Limit: <span className="font-semibold text-foreground">
                            {formatCurrency(budget.limitAmount)}
                          </span>
                        </span>
                      </div>

                      <div className={`text-sm font-medium ${
                        isOverBudget
                          ? "text-rose-500"
                          : budget.remainingAmount > 0
                          ? "text-emerald-500"
                          : "text-muted-foreground"
                      }`}>
                        {isOverBudget
                          ? `Over budget by ${formatCurrency(Math.abs(budget.remainingAmount))}`
                          : `${formatCurrency(budget.remainingAmount)} remaining`}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </motion.div>
            );
          })}
        </div>
      ) : (
        <Card>
          <CardContent className="py-16 text-center text-muted-foreground">
            <PiggyBank className="w-12 h-12 mx-auto mb-3 opacity-50" />
            <p className="font-medium">No budgets yet</p>
            <p className="text-sm">Create your first budget to track spending</p>
          </CardContent>
        </Card>
      )}
    </motion.div>
  );
}
