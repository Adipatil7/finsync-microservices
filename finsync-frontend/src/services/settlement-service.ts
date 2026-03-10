import api from "@/lib/api";
import type {
  BalanceResponse,
  SettlementSuggestion,
  RecordSettlementRequest,
} from "@/types";

export const settlementService = {
  getBalances: async (groupId: string): Promise<BalanceResponse[]> => {
    const response = await api.get<BalanceResponse[]>(
      `/api/settlements/groups/${groupId}/balances`
    );
    return response.data;
  },

  getSettlementPlan: async (groupId: string): Promise<SettlementSuggestion[]> => {
    const response = await api.get<SettlementSuggestion[]>(
      `/api/settlements/groups/${groupId}/settlement-plan`
    );
    return response.data;
  },

  recordSettlement: async (groupId: string, data: RecordSettlementRequest): Promise<string> => {
    const response = await api.post<string>(
      `/api/settlements/groups/${groupId}/settle`,
      data
    );
    return response.data;
  },
};
