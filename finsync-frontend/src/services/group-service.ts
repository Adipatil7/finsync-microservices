import api from "@/lib/api";
import type {
  CreateGroupRequest,
  GroupResponse,
  AddMemberRequest,
  CreateExpenseRequest,
  GroupExpense,
  GroupMember,
} from "@/types";

export const groupService = {
  // ---- Groups ----
  getGroups: async (): Promise<GroupResponse[]> => {
    const response = await api.get<GroupResponse[]>("/api/groups-root");
    return response.data;
  },

  createGroup: async (data: CreateGroupRequest): Promise<GroupResponse> => {
    const response = await api.post<GroupResponse>("/api/groups-root", data);
    return response.data;
  },

  getGroupById: async (id: string): Promise<GroupResponse> => {
    const response = await api.get<GroupResponse>(`/api/groups/${id}`);
    return response.data;
  },

  updateGroup: async (id: string, name: string): Promise<GroupResponse> => {
    const response = await api.put<GroupResponse>(`/api/groups/${id}`, { name });
    return response.data;
  },

  deleteGroup: async (id: string): Promise<void> => {
    await api.delete(`/api/groups/${id}`);
  },

  // ---- Members ----
  getMembers: async (groupId: string): Promise<GroupMember[]> => {
    const response = await api.get<GroupMember[]>(`/api/groups/${groupId}/members`);
    return response.data;
  },

  addMember: async (groupId: string, data: AddMemberRequest): Promise<void> => {
    await api.post(`/api/groups/${groupId}/members`, data);
  },

  removeMember: async (groupId: string, userId: string): Promise<void> => {
    await api.delete(`/api/groups/${groupId}/members/${userId}`);
  },

  // ---- Expenses ----
  getExpenses: async (groupId: string): Promise<GroupExpense[]> => {
    const response = await api.get<GroupExpense[]>(`/api/groups/${groupId}/expenses`);
    return response.data;
  },

  createExpense: async (groupId: string, data: CreateExpenseRequest): Promise<void> => {
    await api.post(`/api/groups/${groupId}/expenses`, data);
  },
};
