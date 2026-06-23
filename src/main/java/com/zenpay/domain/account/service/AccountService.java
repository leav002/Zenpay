package com.zenpay.domain.account.service;

import com.zenpay.domain.account.dto.AccountLookupResponse;
import com.zenpay.domain.account.dto.AccountResponse;
import com.zenpay.domain.account.dto.DepositRequest;
import com.zenpay.domain.account.dto.TransferRequest;
import com.zenpay.domain.account.entity.Account;
import com.zenpay.domain.account.repository.AccountRepository;
import com.zenpay.domain.transaction.entity.Transaction;
import com.zenpay.domain.transaction.repository.TransactionRepository;
import com.zenpay.domain.user.entity.User;
import com.zenpay.domain.user.repository.UserRepository;
import com.zenpay.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // 계좌 개설
    @Transactional
    public AccountResponse createAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("유저를 찾을 수 없습니다."));

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .build();

        return AccountResponse.from(accountRepository.save(account));
    }

    // 내 계좌 목록 조회
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(Long userId) {
        return accountRepository.findAllByUserId(userId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    // 계좌번호로 계좌 조회 (송금 상대 확인용)
    @Transactional(readOnly = true)
    public AccountLookupResponse findByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> BusinessException.notFound("계좌를 찾을 수 없습니다."));
        return AccountLookupResponse.from(account);
    }

    // 충전 (입금)
    @Transactional
    public void deposit(Long userId, Long accountId, DepositRequest request) {
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> BusinessException.notFound("계좌를 찾을 수 없습니다."));

        if (!account.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("본인 계좌만 충전할 수 있습니다.");
        }

        Transaction transaction = Transaction.ofDeposit(account, request.getAmount(), request.getDescription());

        try {
            account.deposit(request.getAmount());
            transaction.complete();
        } catch (BusinessException e) {
            transaction.fail();
            throw e;
        } finally {
            transactionRepository.save(transaction);
        }
    }

    // 송금
    @Transactional
    public void transfer(Long userId, TransferRequest request) {
        // 비관적 락으로 두 계좌 동시에 잠금 (데드락 방지: 항상 id 작은 것부터)
        Long fromId = request.getSenderAccountId();
        Long toId = request.getReceiverAccountId();

        Account sender = accountRepository.findByIdWithLock(Math.min(fromId, toId))
                .orElseThrow(() -> BusinessException.notFound("계좌를 찾을 수 없습니다."));
        Account receiver = accountRepository.findByIdWithLock(Math.max(fromId, toId))
                .orElseThrow(() -> BusinessException.notFound("계좌를 찾을 수 없습니다."));

        // sender/receiver 순서 보정
        if (!sender.getId().equals(fromId)) {
            Account temp = sender;
            sender = receiver;
            receiver = temp;
        }

        // 본인 계좌인지 확인
        if (!sender.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("본인 계좌만 송금할 수 있습니다.");
        }

        // 자기 자신에게 송금 방지
        if (sender.getId().equals(receiver.getId())) {
            throw BusinessException.badRequest("같은 계좌로는 송금할 수 없습니다.");
        }

        Transaction transaction = Transaction.ofTransfer(sender, receiver,
                request.getAmount(), request.getDescription());

        try {
            sender.withdraw(request.getAmount());
            receiver.deposit(request.getAmount());
            transaction.complete();
        } catch (BusinessException e) {
            transaction.fail();
            throw e;
        } finally {
            transactionRepository.save(transaction);
        }
    }

    // 계좌번호 생성 (중복 없을 때까지 재시도)
    private String generateAccountNumber() {
        String number;
        do {
            number = "ZP" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}