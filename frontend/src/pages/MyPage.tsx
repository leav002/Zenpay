import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import * as userApi from "../api/user";
import type { UserResponse } from "../api/user";
import { getErrorMessage } from "../api/errors";

export function MyPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = useState<UserResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    userApi
      .getMe()
      .then(setUser)
      .catch((err) => setError(getErrorMessage(err, "내 정보를 불러오지 못했습니다.")));
  }, []);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <button className="text-button" onClick={() => navigate("/")}>
          ← 뒤로
        </button>
      </header>

      <h1>마이페이지</h1>

      {error && <p className="error">{error}</p>}

      {user && (
        <section className="profile-card">
          <div className="profile-row">
            <span className="profile-label">이름</span>
            <span className="profile-value">{user.name}</span>
          </div>
          <div className="profile-row">
            <span className="profile-label">이메일</span>
            <span className="profile-value">{user.email}</span>
          </div>
          <div className="profile-row">
            <span className="profile-label">전화번호</span>
            <span className="profile-value">{user.phone}</span>
          </div>
        </section>
      )}

      <button className="secondary" onClick={handleLogout}>
        로그아웃
      </button>
    </div>
  );
}
