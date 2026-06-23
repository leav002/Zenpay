import { createContext, useContext, useState, type ReactNode } from "react";
import * as authApi from "../api/auth";
import { getAccessToken, setTokens, clearTokens } from "./tokenStorage";

interface AuthContextValue {
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, name: string, phone: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getAccessToken());

  async function login(email: string, password: string) {
    const { accessToken, refreshToken } = await authApi.login({ email, password });
    setTokens(accessToken, refreshToken);
    setIsAuthenticated(true);
  }

  async function signup(email: string, password: string, name: string, phone: string) {
    await authApi.signup({ email, password, name, phone });
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      clearTokens();
      setIsAuthenticated(false);
    }
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
