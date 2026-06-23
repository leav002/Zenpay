import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import * as accountApi from "../api/account";
import type { AccountLookupResponse, AccountResponse } from "../api/account";
import { getErrorMessage } from "../api/errors";

export function TransferPage() {
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [senderAccountId, setSenderAccountId] = useState("");
  const [receiverAccountNumber, setReceiverAccountNumber] = useState("");
  const [resolvedReceiver, setResolvedReceiver] = useState<AccountLookupResponse | null>(null);
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [isLookingUp, setIsLookingUp] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    accountApi.getMyAccounts().then((list) => {
      setAccounts(list);
      if (list.length > 0) setSenderAccountId(String(list[0].id));
    });
  }, []);

  function handleReceiverInputChange(value: string) {
    setReceiverAccountNumber(value);
    setResolvedReceiver(null);
    setLookupError(null);
  }

  async function handleLookup() {
    if (!receiverAccountNumber) return;
    setIsLookingUp(true);
    setLookupError(null);
    try {
      const result = await accountApi.lookupAccount(receiverAccountNumber);
      setResolvedReceiver(result);
    } catch (err) {
      setLookupError(getErrorMessage(err, "계좌를 찾을 수 없습니다."));
    } finally {
      setIsLookingUp(false);
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!resolvedReceiver) return;
    setError(null);
    setIsSubmitting(true);
    try {
      await accountApi.transfer({
        senderAccountId: Number(senderAccountId),
        receiverAccountId: resolvedReceiver.id,
        amount: Number(amount),
        description: description || undefined,
      });
      navigate(`/accounts/${senderAccountId}`);
    } catch (err) {
      setError(getErrorMessage(err, "송금에 실패했습니다."));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <header className="dashboard-header">
        <button className="text-button" onClick={() => navigate("/")}>
          ← 뒤로
        </button>
      </header>
      <h1>송금하기</h1>
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="senderAccountId">보내는 계좌</label>
          <select
            id="senderAccountId"
            value={senderAccountId}
            onChange={(e) => setSenderAccountId(e.target.value)}
            required
          >
            {accounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.accountNumber} ({a.balance.toLocaleString("ko-KR")}원)
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="receiverAccountNumber">받는 계좌번호</label>
          <div className="lookup-row">
            <input
              id="receiverAccountNumber"
              type="text"
              placeholder="ZP로 시작하는 계좌번호"
              value={receiverAccountNumber}
              onChange={(e) => handleReceiverInputChange(e.target.value)}
              required
            />
            <button
              type="button"
              className="secondary"
              onClick={handleLookup}
              disabled={!receiverAccountNumber || isLookingUp}
            >
              {isLookingUp ? "확인 중" : "확인"}
            </button>
          </div>
          {resolvedReceiver && (
            <p className="lookup-success">{resolvedReceiver.accountNumber} 계좌 확인됨</p>
          )}
          {lookupError && <p className="error">{lookupError}</p>}
        </div>
        <div className="field">
          <label htmlFor="amount">금액</label>
          <input
            id="amount"
            type="number"
            placeholder="0"
            min={1}
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="description">메모 (선택)</label>
          <input
            id="description"
            type="text"
            placeholder="메모를 입력해주세요"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={isSubmitting || !resolvedReceiver || accounts.length === 0}>
          {isSubmitting ? "송금 중..." : "송금하기"}
        </button>
      </form>
    </div>
  );
}
