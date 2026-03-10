// ============================================================
// TypeScript interfaces matching all backend DTOs exactly
// ============================================================

// ---- Enums ----

export enum TransactionType {
  INCOME = "INCOME",
  EXPENSE = "EXPENSE",
}

export enum BudgetPeriod {
  MONTHLY = "MONTHLY",
  YEARLY = "YEARLY",
  WEEKLY = "WEEKLY",
}

export enum SplitType {
  EQUAL = "EQUAL",
  EXACT = "EXACT",
  PERCENTAGE = "PERCENTAGE",
}

// ---- Auth DTOs ----

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterUserRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  name: string;
}

export interface RegisterUserResponse {
  id: string;
  name: string;
  email: string;
}

// ---- Personal Finance DTOs ----

export interface TransactionRequest {
  categoryId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  transactionDate: string; // ISO date "YYYY-MM-DD"
}

export interface TransactionResponse {
  id: string;
  userId: string;
  categoryId: string;
  type: TransactionType;
  amount: number;
  description: string;
  transactionDate: string; // ISO date "YYYY-MM-DD"
  createdAt: string;
  updatedAt: string;
  categoryName: string;
}

export interface CategoryRequest {
  name: string;
  type: TransactionType;
}

export interface CategoryResponse {
  id: string;
  userId: string;
  name: string;
  type: TransactionType;
  createdAt: string;
}

export interface BudgetRequest {
  categoryId?: string | null; // nullable for overall budgets
  limitAmount: number;
  period: BudgetPeriod;
}

export interface BudgetResponse {
  id: string;
  userId: string;
  categoryId: string | null;
  limitAmount: number;
  period: BudgetPeriod;
  createdAt: string;
  categoryName: string;
  spentAmount: number;
  remainingAmount: number;
}

export interface MonthlySummaryResponse {
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  topCategoryName: string;
  topCategoryAmount: number;
}

export interface CalendarDayExpenseResponse {
  date: string; // ISO date "YYYY-MM-DD"
  totalExpenseAmount: number;
  expenses: TransactionResponse[];
}

// ---- Group DTOs ----

export interface CreateGroupRequest {
  name: string;
  description?: string;
  currency?: string;
  createdBy: string;
}

export interface GroupResponse {
  id: string;
  name: string;
  description: string;
  currency: string;
  createdBy: string;
  createdAt: string;
  memberCount: number;
  totalSpent: number;
}

export interface AddMemberRequest {
  userId: string;
  role: string;
}

export interface ExpenseSplitRequest {
  userId: string;
  amount: number;
}

export interface CreateExpenseRequest {
  paidBy: string;
  amount: number;
  currency: string;
  description?: string;
  category?: string;
  expenseDate: string; // ISO date "YYYY-MM-DD"
  splitType: SplitType;
  splits: ExpenseSplitRequest[];
}

export interface GroupExpense {
  id: string;
  groupId: string;
  paidBy: string;
  amount: number;
  currency: string;
  category: string;
  description: string;
  expenseDate: string; // ISO date "YYYY-MM-DD"
  splitType: SplitType;
  createdAt: string;
  updatedAt: string;
}

export interface GroupMember {
  id: string;
  groupId: string;
  userId: string;
  role: string;
  joinedAt: string;
  name?: string; // added to match GroupMemberResponse from backend
}

// ---- Settlement DTOs ----

export interface BalanceResponse {
  debtorId: string;
  creditorId: string;
  amount: number;
  currency: string;
}

export interface SettlementSuggestion {
  fromUserId: string;
  toUserId: string;
  amount: number;
  currency: string;
}

export interface RecordSettlementRequest {
  payerId: string;
  payeeId: string;
  amount: number;
  currency: string;
}
