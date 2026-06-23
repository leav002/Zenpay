# ZenPay

간단한 계좌·송금 서비스. Spring Boot 백엔드와 React 프론트엔드로 구성된 풀스택 토이 프로젝트입니다.

## 기술 스택

**Backend**
- Java 17, Spring Boot 3.3 (Gradle)
- Spring Security + JWT (access/refresh 분리, Redis 기반 RTR)
- Spring Data JPA (MySQL) / H2 (테스트)
- Spring Data Redis (refresh token 저장)
- Bean Validation, springdoc-openapi(Swagger UI)

**Frontend**
- React 19 + TypeScript + Vite
- React Router, Axios

## 기능

| 도메인 | 기능 |
|---|---|
| 인증 | 회원가입, 로그인, 토큰 재발급(RTR), 로그아웃 |
| 계좌 | 계좌 개설, 내 계좌 목록, 계좌번호로 상대 계좌 조회, 충전 |
| 송금 | 계좌 간 송금 (동시성 락, 잔액/소유권 검증) |
| 거래내역 | 계좌별 거래내역 페이지네이션 조회 (상대방 이름 포함) |
| 마이페이지 | 내 정보 조회 |

## 아키텍처 / 설계 메모

도메인별로 패키지를 나눈 구조입니다 (`domain/user`, `domain/account`, `domain/transaction`). 아래는 구현하면서 의도적으로 선택한 부분들입니다.

### 1. 송금 동시성 제어

두 계좌 간 동시 송금에서 흔히 발생하는 데드락을 막기 위해, `senderAccountId`/`receiverAccountId` 중 **항상 더 작은 ID를 먼저 비관적 락(`PESSIMISTIC_WRITE`)으로 잠그고** 그 다음 큰 ID를 잠급니다. 두 트랜잭션이 서로 다른 순서로 락을 거는 상황 자체를 차단해서, 별도의 타임아웃/재시도 로직 없이 데드락을 회피합니다.

```java
Account sender = accountRepository.findByIdWithLock(Math.min(fromId, toId))...
Account receiver = accountRepository.findByIdWithLock(Math.max(fromId, toId))...
```

락을 건 뒤 실제 sender/receiver 역할을 다시 매칭하고, 본인 계좌 여부와 자기 자신에게 송금하는 경우를 검증합니다. 트랜잭션 실패 시 `Transaction` 엔티티를 `FAILED` 상태로 남겨서 실패 이력도 추적할 수 있게 했습니다.

### 2. JWT Refresh Token Rotation (RTR)

Access token은 짧은 만료시간으로 발급하고, Refresh token은 Redis에 `refresh:{userId}` 키로 저장합니다. `/auth/reissue` 호출 시:
1. 들어온 refresh token이 유효한지, Redis에 저장된 값과 일치하는지 확인
2. 일치하면 access/refresh 토큰을 **둘 다 새로 발급**하고 Redis 값을 새 refresh token으로 교체

기존 refresh token은 그 즉시 무효가 되므로, 탈취된 토큰이 재사용되는 걸 막을 수 있습니다. 로그아웃은 Redis에서 해당 키를 삭제하는 것으로 처리합니다.

### 3. 계좌번호 기반 송금

`TransferRequest`는 내부 계좌 ID를 받지만, 사용자가 직접 알기 어려운 값이라 프론트엔드에서 `GET /accounts/lookup?accountNumber=`로 먼저 상대 계좌를 조회해 ID로 변환한 뒤 송금을 요청하는 2단계 흐름으로 구성했습니다.

### 4. 환경변수 분리

DB 비밀번호, JWT 시크릿 등은 `application.yml`에 하드코딩하지 않고 `${VAR:default}` 형태로 환경변수에서 주입받습니다. `.env.example`에 필요한 변수 목록을 정리해뒀습니다.

## 한계 / TODO

- **테스트 코드 없음** — 동시성 락, 잔액 검증처럼 테스트로 보여주기 좋은 로직이 비어있는 상태. 다음 작업 우선순위.
- 계좌 충전(`/accounts/{id}/deposit`)은 실제 결제 연동 없이 금액을 바로 적립하는 mock입니다.
- 거래내역의 `senderName`/`receiverName`은 매 요청마다 지연 로딩으로 조회해서, 페이지 크기가 커지면 N+1 쿼리가 발생할 수 있습니다.
- 계좌 정지/폐쇄 관리, 비밀번호 변경 등 마이페이지 부가 기능은 아직 없습니다.

## 로컬 실행

### Backend

MySQL, Redis가 필요합니다 (docker로 띄우는 걸 추천).

```bash
DB_PASSWORD=<mysql-root-password> \
JWT_SECRET=<32자 이상의 임의 문자열> \
./gradlew bootRun
```

IntelliJ에서 실행할 경우 Run Configuration의 Environment variables에 위 값을 등록해야 합니다.

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

기본적으로 `http://localhost:5173`에서 실행되고, `http://localhost:8081` 백엔드를 호출합니다.

## API 문서

백엔드 실행 후 `http://localhost:8081/swagger-ui.html` 에서 확인할 수 있습니다.
