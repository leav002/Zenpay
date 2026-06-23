package com.zenpay.domain.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferRequest {

    @NotNull(message = "출금 계좌 ID를 입력해주세요.")
    private Long senderAccountId;

    @NotNull(message = "입금 계좌 ID를 입력해주세요.")
    private Long receiverAccountId;

    @NotNull(message = "금액을 입력해주세요.")
    @DecimalMin(value = "1", message = "최소 송금액은 1원입니다.")
    private BigDecimal amount;

    private String description;
}