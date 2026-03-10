import api from "@/lib/api";
import type {
  TransactionRequest,
  TransactionResponse,
  BudgetRequest,
  BudgetResponse,
  CategoryRequest,
  CategoryResponse,
  MonthlySummaryResponse,
  CalendarDayExpenseResponse,
} from "@/types";

export const personalService = {
  // ---- Transactions ----
  getTransactions: async (from?: string, to?: string): Promise<TransactionResponse[]> => {
    const params: Record<string, string> = {};
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await api.get<TransactionResponse[]>("/api/personal/transactions", { params });
    return response.data;
  },

  createTransaction: async (data: TransactionRequest): Promise<TransactionResponse> => {
    const response = await api.post<TransactionResponse>("/api/personal/transactions", data);
    return response.data;
  },

  deleteTransaction: async (id: string): Promise<void> => {
    await api.delete(`/api/personal/transactions/${id}`);
  },

  // ---- Budgets ----
  getBudgets: async (): Promise<BudgetResponse[]> => {
    const response = await api.get<BudgetResponse[]>("/api/personal/budgets");
    return response.data;
  },

  createBudget: async (data: BudgetRequest): Promise<BudgetResponse> => {
    const response = await api.post<BudgetResponse>("/api/personal/budgets", data);
    return response.data;
  },

  deleteBudget: async (id: string): Promise<void> => {
    await api.delete(`/api/personal/budgets/${id}`);
  },

  // ---- Categories ----
  getCategories: async (): Promise<CategoryResponse[]> => {
    const response = await api.get<CategoryResponse[]>("/api/personal/categories");
    return response.data;
  },

  createCategory: async (data: CategoryRequest): Promise<CategoryResponse> => {
    const response = await api.post<CategoryResponse>("/api/personal/categories", data);
    return response.data;
  },

  deleteCategory: async (id: string): Promise<void> => {
    await api.delete(`/api/personal/categories/${id}`);
  },

  // ---- Analytics ----
  getMonthlySummary: async (month: string): Promise<MonthlySummaryResponse> => {
    const response = await api.get<MonthlySummaryResponse>("/api/personal/summary", {
      params: { month },
    });
    return response.data;
  },

  getCalendarExpenses: async (month: string): Promise<CalendarDayExpenseResponse[]> => {
    const response = await api.get<CalendarDayExpenseResponse[]>("/api/personal/calendar", {
      params: { month },
    });
    return response.data;
  },
};
