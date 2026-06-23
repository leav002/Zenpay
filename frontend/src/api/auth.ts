import { apiClient } from "./client";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export async function login(request: LoginRequest): Promise<TokenResponse> {
  const res = await apiClient.post<ApiResponse<TokenResponse>>("/auth/login", request);
  return res.data.data;
}

export async function signup(request: SignupRequest): Promise<void> {
  await apiClient.post<ApiResponse<null>>("/auth/signup", request);
}

export async function logout(): Promise<void> {
  await apiClient.delete<ApiResponse<null>>("/auth/logout");
}
