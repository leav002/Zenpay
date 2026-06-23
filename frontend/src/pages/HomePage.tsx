import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as accountApi from "../api/account";
import type { AccountResponse } from "../api/account";
import { getErrorMessage } from "../api/errors";
import { formatWon } from "../utils/format";

export function HomePage() {
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);

  useEffect(() => {
    loadAccounts();
  }, []);

  async function loadAccounts() {
    setIsLoading(true);
    setError(null);
    try {
      setAccounts(await accountApi.getMyAccounts());
    } catch (err) {
      setError(getErrorMessage(err, "계좌 목록을 불러오지 못했습니다."));
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCreateAccount() {
    setIsCreating(true);
    setError(null);
    try {
      await accountApi.createAccount();
      await loadAccounts();
    } catch (err) {
      setError(getErrorMessage(err, "계좌 개설에 실패했습니다."));
    } finally {
      setIsCreating(false);
    }
  }

  const totalBalance = accounts.reduce((sum, a) => sum + a.balance, 0);

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="brand">ZenPay</div>
        <button className="text-button" onClick={() => navigate("/mypage")}>
          마이페이지
        </button>
      </header>

      <section className="total-balance">
        <p className="label">총 자산</p>
        <p className="amount">{formatWon(totalBalance)}</p>
      </section>

      {error && <p className="error">{error}</p>}

      <section className="account-list">
        {isLoading ? (
          <p className="empty">불러오는 중...</p>
        ) : accounts.length === 0 ? (
          <p className="empty">아직 개설된 계좌가 없어요.</p>
        ) : (
          accounts.map((account) => (
            <button
              key={account.id}
              className="account-card"
              onClick={() => navigate(`/accounts/${account.id}`)}
            >
              <div>
                <p className="account-number">{account.accountNumber}</p>
                <p className="account-status">
                  {account.status === "ACTIVE" ? "정상" : account.status === "SUSPENDED" ? "정지" : "폐쇄"}
                </p>
              </div>
              <p className="account-balance">{formatWon(account.balance)}</p>
            </button>
          ))
        )}
      </section>

      <div className="action-row">
        <button onClick={handleCreateAccount} disabled={isCreating} className="secondary">
          {isCreating ? "개설 중..." : "+ 계좌 개설"}
        </button>
        <button onClick={() => navigate("/transfer")} disabled={accounts.length === 0}>
          송금하기
        </button>
      </div>
    </div>
  );
}
