package com.zenpay.domain.transaction.service;

import com.zenpay.domain.account.entity.Account;
import com.zenpay.domain.account.repository.AccountRepository;
import com.zenpay.domain.transaction.dto.TransactionResponse;
import com.zenpay.domain.transaction.repository.TransactionRepository;
import com.zenpay.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long userId, Long accountId, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> BusinessException.notFound("계좌를 찾을 수 없습니다."));

        if (!account.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("본인 계좌의 거래 내역만 조회할 수 있습니다.");
        }

        return transactionRepository.findAllByAccountId(accountId, pageable)
                .map(TransactionResponse::from);
    }
}