"use client";

import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { toast } from "sonner";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Receipt,
  Users,
  ArrowLeftRight,
  UserPlus,
  Trash2,
  Plus,
  Loader2,
  ArrowRight,
  DollarSign,
  CheckCircle2,
} from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
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
import { groupService } from "@/services/group-service";
import { settlementService } from "@/services/settlement-service";
import { useAuthStore } from "@/lib/auth-store";
import { formatCurrency, formatDate } from "@/lib/utils";
import { SplitType } from "@/types";
import type { GroupMember } from "@/types";

// Schemas
const expenseSchema = z
  .object({
    amount: z.coerce.number().positive("Amount must be positive"),
    currency: z.string().min(1, "Currency is required"),
    description: z.string().optional(),
    category: z.string().optional(),
    expenseDate: z.string().min(1, "Date is required"),
    splitType: z.nativeEnum(SplitType),
    splits: z
      .array(
        z.object({
          userId: z.string(),
          amount: z.coerce.number().min(0),
        }),
      )
      .optional(),
  })
  .refine(
    (data) => {
      if (data.splitType === SplitType.EXACT) {
        const totalSplit =
          data.splits?.reduce((sum, split) => sum + split.amount, 0) || 0;
        return Math.abs(totalSplit - data.amount) < 0.01;
      }
      if (data.splitType === SplitType.PERCENTAGE) {
        const totalPercent =
          data.splits?.reduce((sum, split) => sum + split.amount, 0) || 0;
        return Math.abs(totalPercent - 100) < 0.01;
      }
      return true;
    },
    {
      message:
        "Splits must sum up to the total amount (for EXACT) or 100 (for PERCENTAGE)",
      path: ["splits"],
    },
  );

const memberSchema = z.object({
  userId: z.string().min(1, "User ID is required"),
  role: z.string().min(1, "Role is required"),
});

type ExpenseFormData = z.infer<typeof expenseSchema>;
type MemberFormData = z.infer<typeof memberSchema>;

export default function GroupDetailPage() {
  const params = useParams();
  const groupId = params.id as string;
  const queryClient = useQueryClient();
  const { userId } = useAuthStore();
  const [expenseDialog, setExpenseDialog] = useState(false);
  const [memberDialog, setMemberDialog] = useState(false);

  // Queries
  const { data: group, isLoading } = useQuery({
    queryKey: ["group", groupId],
    queryFn: () => groupService.getGroupById(groupId),
  });

  const { data: expenses } = useQuery({
    queryKey: ["group-expenses", groupId],
    queryFn: () => groupService.getExpenses(groupId),
  });

  const { data: members } = useQuery({
    queryKey: ["group-members", groupId],
    queryFn: () => groupService.getMembers(groupId),
  });

  const { data: balances } = useQuery({
    queryKey: ["group-balances", groupId],
    queryFn: () => settlementService.getBalances(groupId),
  });

  const { data: settlementPlan } = useQuery({
    queryKey: ["settlement-plan", groupId],
    queryFn: () => settlementService.getSettlementPlan(groupId),
  });

  // Mutations
  const addExpenseMutation = useMutation({
    mutationFn: (data: ExpenseFormData) => {
      // For EQUAL split, divide equally among all members
      let splits = [];

      if (data.splitType === SplitType.EQUAL) {
        const memberList = members || [];
        splits = memberList.map((m) => ({
          userId: m.userId,
          amount: Math.round((data.amount / memberList.length) * 100) / 100,
        }));
      } else {
        splits = data.splits || [];
      }

      return groupService.createExpense(groupId, {
        paidBy: userId!,
        amount: data.amount,
        currency: data.currency,
        description: data.description,
        category: data.category,
        expenseDate: data.expenseDate,
        splitType: data.splitType,
        splits,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-expenses", groupId] });
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
      queryClient.invalidateQueries({ queryKey: ["group-balances", groupId] });
      queryClient.invalidateQueries({ queryKey: ["settlement-plan", groupId] });
      toast.success("Expense added");
      setExpenseDialog(false);
    },
    onError: (err: any) => {
      toast.error("Failed to add expense", {
        description: err.response?.data?.message || "Please try again",
      });
    },
  });

  const addMemberMutation = useMutation({
    mutationFn: (data: MemberFormData) => groupService.addMember(groupId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-members", groupId] });
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
      toast.success("Member added");
      setMemberDialog(false);
    },
    onError: (err: any) => {
      toast.error("Failed to add member", {
        description: err.response?.data?.message || "Please try again",
      });
    },
  });

  const removeMemberMutation = useMutation({
    mutationFn: (memberId: string) =>
      groupService.removeMember(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-members", groupId] });
      queryClient.invalidateQueries({ queryKey: ["group", groupId] });
      toast.success("Member removed");
    },
    onError: () => toast.error("Failed to remove member"),
  });

  const settleMutation = useMutation({
    mutationFn: (data: {
      payerId: string;
      payeeId: string;
      amount: number;
      currency: string;
    }) => settlementService.recordSettlement(groupId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["group-balances", groupId] });
      queryClient.invalidateQueries({ queryKey: ["settlement-plan", groupId] });
      toast.success("Settlement recorded");
    },
    onError: () => toast.error("Failed to record settlement"),
  });

  // Forms
  const expenseForm = useForm<ExpenseFormData>({
    resolver: zodResolver(expenseSchema),
    defaultValues: {
      currency: group?.currency || "INR",
      splitType: SplitType.EQUAL,
      expenseDate: new Date().toISOString().split("T")[0],
      splits: [],
    },
  });

  // Re-initialize splits when splitType changes or members load
  const currentSplitType = expenseForm.watch("splitType");
  useState(() => {
    if (
      members &&
      currentSplitType !== SplitType.EQUAL &&
      (!expenseForm.getValues("splits") ||
        expenseForm.getValues("splits")?.length === 0)
    ) {
      const initialSplits = members.map((m) => ({
        userId: m.userId,
        amount: 0,
      }));
      expenseForm.setValue("splits", initialSplits);
    }
  });

  const handleSplitTypeChange = (val: SplitType) => {
    expenseForm.setValue("splitType", val);
    if (val !== SplitType.EQUAL && members) {
      expenseForm.setValue(
        "splits",
        members.map((m) => ({ userId: m.userId, amount: 0 })),
      );
    } else {
      expenseForm.setValue("splits", []);
    }
  };

  const memberForm = useForm<MemberFormData>({
    resolver: zodResolver(memberSchema),
    defaultValues: { role: "MEMBER" },
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  // Helper to display user name or fallback to short ID
  const getUserName = (id: string) => {
    const member = members?.find((m) => m.userId === id);
    if (member && member.name) return member.name;
    return id.substring(0, 8) + "...";
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6"
    >
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{group?.name}</h1>
          {group?.description && (
            <p className="text-muted-foreground mt-1">{group.description}</p>
          )}
          <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
            <span className="flex items-center gap-1">
              <Users className="w-4 h-4" /> {group?.memberCount} members
            </span>
            <span className="flex items-center gap-1">
              <DollarSign className="w-4 h-4" />{" "}
              {formatCurrency(group?.totalSpent || 0, group?.currency || "INR")}{" "}
              total
            </span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="expenses">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="expenses" className="gap-2">
            <Receipt className="w-4 h-4" /> Expenses
          </TabsTrigger>
          <TabsTrigger value="balances" className="gap-2">
            <DollarSign className="w-4 h-4" /> Balances
          </TabsTrigger>
          <TabsTrigger value="settlements" className="gap-2">
            <ArrowLeftRight className="w-4 h-4" /> Settlements
          </TabsTrigger>
          <TabsTrigger value="members" className="gap-2">
            <Users className="w-4 h-4" /> Members
          </TabsTrigger>
        </TabsList>

        {/* Expenses Tab */}
        <TabsContent value="expenses" className="space-y-4">
          <div className="flex justify-end">
            <Dialog
              open={expenseDialog}
              onOpenChange={(open) => {
                setExpenseDialog(open);
                if (open) expenseForm.reset();
              }}
            >
              <DialogTrigger asChild>
                <Button className="gap-2">
                  <Plus className="w-4 h-4" /> Add Expense
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Add Group Expense</DialogTitle>
                  <DialogDescription>
                    This expense will be split among all members
                  </DialogDescription>
                </DialogHeader>
                <form
                  onSubmit={expenseForm.handleSubmit((data) =>
                    addExpenseMutation.mutate(data),
                  )}
                  className="space-y-4"
                >
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label>Amount</Label>
                      <Input
                        type="number"
                        step="0.01"
                        placeholder="0.00"
                        {...expenseForm.register("amount")}
                      />
                      {expenseForm.formState.errors.amount && (
                        <p className="text-sm text-destructive">
                          {expenseForm.formState.errors.amount.message}
                        </p>
                      )}
                    </div>
                    <div className="space-y-2">
                      <Label>Currency</Label>
                      <Input {...expenseForm.register("currency")} />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label>Category</Label>
                    <Input
                      placeholder="Food, Transport, etc."
                      {...expenseForm.register("category")}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Description</Label>
                    <Input
                      placeholder="Dinner at restaurant"
                      {...expenseForm.register("description")}
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label>Date</Label>
                      <Input
                        type="date"
                        {...expenseForm.register("expenseDate")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Split Type</Label>
                      <Select
                        value={expenseForm.watch("splitType")}
                        onValueChange={handleSplitTypeChange}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="EQUAL">Equal</SelectItem>
                          <SelectItem value="EXACT">Exact</SelectItem>
                          <SelectItem value="PERCENTAGE">Percentage</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                  {currentSplitType !== SplitType.EQUAL && members && (
                    <div className="space-y-3 pt-2">
                      <Label className="text-sm font-semibold">
                        Split Breakdown
                      </Label>
                      <div className="grid gap-3">
                        {members.map((m, index) => (
                          <div
                            key={m.userId}
                            className="flex items-center justify-between gap-4"
                          >
                            <span className="text-sm">
                              {getUserName(m.userId)}
                            </span>
                            <div className="flex items-center gap-2">
                              <Input
                                type="number"
                                step="0.01"
                                className="w-24 h-8"
                                placeholder="0"
                                {...expenseForm.register(
                                  `splits.${index}.amount` as const,
                                )}
                              />
                              <span className="text-sm text-muted-foreground w-4">
                                {currentSplitType === SplitType.PERCENTAGE
                                  ? "%"
                                  : expenseForm.watch("currency")}
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                      {expenseForm.formState.errors.splits && (
                        <p className="text-sm text-destructive">
                          {expenseForm.formState.errors.splits.message}
                        </p>
                      )}
                    </div>
                  )}
                  <DialogFooter>
                    <Button
                      type="submit"
                      disabled={addExpenseMutation.isPending}
                      className="w-full"
                    >
                      {addExpenseMutation.isPending ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />{" "}
                          Adding...
                        </>
                      ) : (
                        "Add Expense"
                      )}
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>

          {expenses && expenses.length > 0 ? (
            <div className="space-y-3">
              {expenses.map((expense) => (
                <Card
                  key={expense.id}
                  className="hover:shadow-md transition-all"
                >
                  <CardContent className="p-4 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-lg bg-indigo-500/10 flex items-center justify-center">
                        <Receipt className="w-5 h-5 text-indigo-500" />
                      </div>
                      <div>
                        <p className="font-medium">
                          {expense.description || expense.category || "Expense"}
                        </p>
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          {expense.category && (
                            <span className="px-2 py-0.5 bg-accent rounded-md text-xs font-medium">
                              {expense.category}
                            </span>
                          )}
                          <span>Paid by: {getUserName(expense.paidBy)}</span>
                          <span>{formatDate(expense.expenseDate)}</span>
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold text-lg">
                        {formatCurrency(expense.amount, expense.currency)}
                      </p>
                      <span className="text-xs text-muted-foreground capitalize">
                        {expense.splitType.toLowerCase()} split
                      </span>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center text-muted-foreground">
                <Receipt className="w-10 h-10 mx-auto mb-2 opacity-50" />
                <p>No expenses yet</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Balances Tab */}
        <TabsContent value="balances" className="space-y-4">
          {balances && balances.length > 0 ? (
            <div className="space-y-3">
              {balances.map((balance, i) => (
                <Card key={i} className="hover:shadow-md transition-all">
                  <CardContent className="p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-rose-500/10 flex items-center justify-center">
                        <ArrowRight className="w-5 h-5 text-rose-500" />
                      </div>
                      <div>
                        <p className="text-sm">
                          <span className="font-medium">
                            {getUserName(balance.debtorId)}
                          </span>
                          <span className="text-muted-foreground"> owes </span>
                          <span className="font-medium">
                            {getUserName(balance.creditorId)}
                          </span>
                        </p>
                      </div>
                    </div>
                    <p className="font-semibold text-lg text-rose-500">
                      {formatCurrency(balance.amount, balance.currency)}
                    </p>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center text-muted-foreground">
                <DollarSign className="w-10 h-10 mx-auto mb-2 opacity-50" />
                <p>All settled up!</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Settlements Tab */}
        <TabsContent value="settlements" className="space-y-4">
          {settlementPlan && settlementPlan.length > 0 ? (
            <div className="space-y-3">
              {settlementPlan.map((suggestion, i) => (
                <Card
                  key={i}
                  className="hover:shadow-md transition-all border-amber-500/20"
                >
                  <CardContent className="p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center">
                        <ArrowLeftRight className="w-5 h-5 text-amber-500" />
                      </div>
                      <div>
                        <p className="text-sm">
                          <span className="font-medium">
                            {getUserName(suggestion.fromUserId)}
                          </span>
                          <span className="text-muted-foreground"> pays </span>
                          <span className="font-medium">
                            {getUserName(suggestion.toUserId)}
                          </span>
                        </p>
                        <p className="text-lg font-semibold text-amber-600">
                          {formatCurrency(
                            suggestion.amount,
                            suggestion.currency,
                          )}
                        </p>
                      </div>
                    </div>
                    <Button
                      size="sm"
                      variant="outline"
                      className="gap-2 border-emerald-500/50 text-emerald-600 hover:bg-emerald-500/10"
                      onClick={() =>
                        settleMutation.mutate({
                          payerId: suggestion.fromUserId,
                          payeeId: suggestion.toUserId,
                          amount: suggestion.amount,
                          currency: suggestion.currency,
                        })
                      }
                      disabled={settleMutation.isPending}
                    >
                      <CheckCircle2 className="w-4 h-4" />
                      Settle
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center text-muted-foreground">
                <CheckCircle2 className="w-10 h-10 mx-auto mb-2 opacity-50 text-emerald-500" />
                <p className="font-medium">All settled up!</p>
                <p className="text-sm">No pending settlements</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Members Tab */}
        <TabsContent value="members" className="space-y-4">
          <div className="flex justify-end">
            <Dialog
              open={memberDialog}
              onOpenChange={(open) => {
                setMemberDialog(open);
                if (open) memberForm.reset();
              }}
            >
              <DialogTrigger asChild>
                <Button variant="outline" className="gap-2">
                  <UserPlus className="w-4 h-4" /> Add Member
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Add Member</DialogTitle>
                  <DialogDescription>
                    Add a user to this group by their ID
                  </DialogDescription>
                </DialogHeader>
                <form
                  onSubmit={memberForm.handleSubmit((data) =>
                    addMemberMutation.mutate(data),
                  )}
                  className="space-y-4"
                >
                  <div className="space-y-2">
                    <Label>User ID</Label>
                    <Input
                      placeholder="Enter user UUID"
                      {...memberForm.register("userId")}
                    />
                    {memberForm.formState.errors.userId && (
                      <p className="text-sm text-destructive">
                        {memberForm.formState.errors.userId.message}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label>Role</Label>
                    <Select
                      value={memberForm.watch("role")}
                      onValueChange={(val) => memberForm.setValue("role", val)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ADMIN">Admin</SelectItem>
                        <SelectItem value="MEMBER">Member</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <DialogFooter>
                    <Button
                      type="submit"
                      disabled={addMemberMutation.isPending}
                      className="w-full"
                    >
                      {addMemberMutation.isPending ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />{" "}
                          Adding...
                        </>
                      ) : (
                        "Add Member"
                      )}
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>

          {members && members.length > 0 ? (
            <div className="space-y-2">
              {members.map((member) => (
                <Card
                  key={member.id}
                  className="hover:shadow-md transition-all"
                >
                  <CardContent className="p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full gradient-primary flex items-center justify-center text-white font-semibold text-sm">
                        {member.userId.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="font-medium">
                          {member.name || getUserName(member.userId)}
                        </p>
                        <span
                          className={`text-xs px-2 py-0.5 rounded-md ${
                            member.role === "ADMIN"
                              ? "bg-indigo-500/10 text-indigo-500"
                              : "bg-accent"
                          }`}
                        >
                          {member.role}
                        </span>
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="text-muted-foreground hover:text-destructive"
                      onClick={() => removeMemberMutation.mutate(member.userId)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="py-12 text-center text-muted-foreground">
                <Users className="w-10 h-10 mx-auto mb-2 opacity-50" />
                <p>No members yet</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </motion.div>
  );
}
