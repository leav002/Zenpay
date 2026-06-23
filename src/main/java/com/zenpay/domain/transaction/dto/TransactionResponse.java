package com.zenpay.domain.transaction.dto;

import com.zenpay.domain.transaction.entity.Transaction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {
    private Long id;
    private Long senderAccountId;
    private String senderName;
    private Long receiverAccountId;
    private String receiverName;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private Transaction.TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .senderAccountId(t.getSenderAccount() != null ? t.getSenderAccount().getId() : null)
                .senderName(t.getSenderAccount() != null ? t.getSenderAccount().getUser().getName() : null)
                .receiverAccountId(t.getReceiverAccount() != null ? t.getReceiverAccount().getId() : null)
                .receiverName(t.getReceiverAccount() != null ? t.getReceiverAccount().getUser().getName() : null)
                .amount(t.getAmount())
                .type(t.getType())
                .status(t.getStatus())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}