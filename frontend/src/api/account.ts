import { apiClient } from "./client";
import type { ApiResponse } from "./auth";

export type AccountStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";

export interface AccountResponse {
  id: number;
  accountNumber: string;
  balance: number;
  status: AccountStatus;
  createdAt: string;
}

export interface TransferRequest {
  senderAccountId: number;
  receiverAccountId: number;
  amount: number;
  description?: string;
}

export interface AccountLookupResponse {
  id: number;
  accountNumber: string;
}

export interface DepositRequest {
  amount: number;
  description?: string;
}

export async function getMyAccounts(): Promise<AccountResponse[]> {
  const res = await apiClient.get<ApiResponse<AccountResponse[]>>("/accounts/me");
  return res.data.data;
}

export async function lookupAccount(accountNumber: string): Promise<AccountLookupResponse> {
  const res = await apiClient.get<ApiResponse<AccountLookupResponse>>("/accounts/lookup", {
    params: { accountNumber },
  });
  return res.data.data;
}

export async function createAccount(): Promise<AccountResponse> {
  const res = await apiClient.post<ApiResponse<AccountResponse>>("/accounts");
  return res.data.data;
}

export async function transfer(request: TransferRequest): Promise<void> {
  await apiClient.post<ApiResponse<null>>("/transfers", request);
}

export async function deposit(accountId: number, request: DepositRequest): Promise<void> {
  await apiClient.post<ApiResponse<null>>(`/accounts/${accountId}/deposit`, request);
}
