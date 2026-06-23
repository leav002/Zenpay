package com.zenpay.domain.transaction.entity;

import com.zenpay.domain.account.entity.Account;
import com.zenpay.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id")
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    private String description;

    public enum TransactionType {
        TRANSFER,   // 계좌 간 송금
        DEPOSIT,    // 충전
        WITHDRAWAL  // 출금
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED
    }

    public void complete() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void fail() {
        this.status = TransactionStatus.FAILED;
    }

    public static Transaction ofTransfer(Account sender, Account receiver,
                                         BigDecimal amount, String description) {
        return Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .description(description)
                .build();
    }

    public static Transaction ofDeposit(Account account, BigDecimal amount, String description) {
        return Transaction.builder()
                .receiverAccount(account)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .description(description)
                .build();
    }
}
