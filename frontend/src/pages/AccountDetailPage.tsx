import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as accountApi from "../api/account";
import * as transactionApi from "../api/transaction";
import type { AccountResponse } from "../api/account";
import type { TransactionResponse } from "../api/transaction";
import { getErrorMessage } from "../api/errors";
import { formatDateTime, formatWon } from "../utils/format";

const STATUS_LABEL: Record<string, string> = {
  PENDING: "처리 중",
  COMPLETED: "완료",
  FAILED: "실패",
};

export function AccountDetailPage() {
  const { accountId } = useParams<{ accountId: string }>();
  const navigate = useNavigate();
  const id = Number(accountId);

  const [account, setAccount] = useState<AccountResponse | null>(null);
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [depositAmount, setDepositAmount] = useState("");
  const [isDepositing, setIsDepositing] = useState(false);
  const [depositError, setDepositError] = useState<string | null>(null);

  useEffect(() => {
    loadAccount();
    loadTransactions(0);
  }, [id]);

  async function loadAccount() {
    try {
      const accounts = await accountApi.getMyAccounts();
      setAccount(accounts.find((a) => a.id === id) ?? null);
    } catch (err) {
      setError(getErrorMessage(err, "계좌 정보를 불러오지 못했습니다."));
    }
  }

  async function loadTransactions(targetPage: number) {
    setIsLoading(true);
    setError(null);
    try {
      const result = await transactionApi.getTransactions(id, targetPage);
      setTransactions((prev) => (targetPage === 0 ? result.content : [...prev, ...result.content]));
      setHasMore(!result.last);
      setPage(targetPage);
    } catch (err) {
      setError(getErrorMessage(err, "거래 내역을 불러오지 못했습니다."));
    } finally {
      setIsLoading(false);
    }
  }

  async function handleDeposit(e: FormEvent) {
    e.preventDefault();
    setDepositError(null);
    setIsDepositing(true);
    try {
      await accountApi.deposit(id, { amount: Number(depositAmount) });
      setDepositAmount("");
      setIsDepositOpen(false);
      await loadAccount();
      await loadTransactions(0);
    } catch (err) {
      setDepositError(getErrorMessage(err, "충전에 실패했습니다."));
    } finally {
      setIsDepositing(false);
    }
  }

  function describe(t: TransactionResponse): string {
    if (t.type === "DEPOSIT") return "계좌 충전";
    if (t.senderAccountId === id) return `${t.receiverName ?? "상대방"}님에게 송금`;
    if (t.receiverAccountId === id) return `${t.senderName ?? "상대방"}님에게 입금받음`;
    return t.description ?? "거래";
  }

  function isOutgoing(t: TransactionResponse): boolean {
    return t.senderAccountId === id;
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <button className="text-button" onClick={() => navigate("/")}>
          ← 뒤로
        </button>
      </header>

      {account && (
        <section className="total-balance">
          <p className="label">{account.accountNumber}</p>
          <p className="amount">{formatWon(account.balance)}</p>
        </section>
      )}

      {error && <p className="error">{error}</p>}

      {isDepositOpen ? (
        <form className="deposit-form" onSubmit={handleDeposit}>
          <div className="field">
            <label htmlFor="depositAmount">충전 금액</label>
            <input
              id="depositAmount"
              type="number"
              placeholder="0"
              min={1}
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
              autoFocus
              required
            />
          </div>
          {depositError && <p className="error">{depositError}</p>}
          <div className="action-row">
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setIsDepositOpen(false);
                setDepositError(null);
              }}
            >
              취소
            </button>
            <button type="submit" disabled={isDepositing}>
              {isDepositing ? "충전 중..." : "충전하기"}
            </button>
          </div>
        </form>
      ) : (
        <button className="secondary" onClick={() => setIsDepositOpen(true)}>
          + 충전하기
        </button>
      )}

      <section className="transaction-list">
        {transactions.length === 0 && !isLoading ? (
          <p className="empty">거래 내역이 없어요.</p>
        ) : (
          transactions.map((t) => (
            <div key={t.id} className="transaction-row">
              <div>
                <p className="transaction-desc">{describe(t)}</p>
                <p className="transaction-date">
                  {formatDateTime(t.createdAt)} · {STATUS_LABEL[t.status]}
                </p>
              </div>
              <p className={isOutgoing(t) ? "transaction-amount minus" : "transaction-amount plus"}>
                {isOutgoing(t) ? "-" : "+"}
                {formatWon(t.amount)}
              </p>
            </div>
          ))
        )}
      </section>

      {hasMore && !isLoading && transactions.length > 0 && (
        <button className="secondary" onClick={() => loadTransactions(page + 1)}>
          더보기
        </button>
      )}
    </div>
  );
}
