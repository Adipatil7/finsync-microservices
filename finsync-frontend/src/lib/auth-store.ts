import { create } from "zustand";
import { persist } from "zustand/middleware";

interface AuthState {
  token: string | null;
  userId: string | null;
  email: string | null;
  name: string | null;
  isAuthenticated: boolean;
  login: (token: string, userId: string, email: string, name: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      email: null,
      name: null,
      isAuthenticated: false,
      login: (token, userId, email, name) =>
        set({ token, userId, email, name, isAuthenticated: true }),
      logout: () =>
        set({ token: null, userId: null, email: null, name: null, isAuthenticated: false }),
    }),
    {
      name: "finsync-auth",
    }
  )
);
