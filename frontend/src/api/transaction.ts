import { apiClient } from "./client";
import type { ApiResponse } from "./auth";

export type TransactionType = "TRANSFER" | "DEPOSIT" | "WITHDRAWAL";
export type TransactionStatus = "PENDING" | "COMPLETED" | "FAILED";

export interface TransactionResponse {
  id: number;
  senderAccountId: number | null;
  senderName: string | null;
  receiverAccountId: number | null;
  receiverName: string | null;
  amount: number;
  type: TransactionType;
  status: TransactionStatus;
  description: string | null;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export async function getTransactions(
  accountId: number,
  page: number,
  size = 20
): Promise<PageResponse<TransactionResponse>> {
  const res = await apiClient.get<ApiResponse<PageResponse<TransactionResponse>>>(
    "/transactions",
    { params: { accountId, page, size } }
  );
  return res.data.data;
}
