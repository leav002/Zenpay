import { apiClient } from "./client";
import type { ApiResponse } from "./auth";

export interface UserResponse {
  id: number;
  email: string;
  name: string;
  phone: string;
}

export async function getMe(): Promise<UserResponse> {
  const res = await apiClient.get<ApiResponse<UserResponse>>("/users/me");
  return res.data.data;
}
