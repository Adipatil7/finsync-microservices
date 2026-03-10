import api from "@/lib/api";
import type { LoginRequest, AuthResponse, RegisterUserRequest, RegisterUserResponse } from "@/types";

export const authService = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>("/api/auth/login", data);
    return response.data;
  },

  register: async (data: RegisterUserRequest): Promise<RegisterUserResponse> => {
    const response = await api.post<RegisterUserResponse>("/api/auth/register", data);
    return response.data;
  },
};
