package com.zenpay.domain.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DepositRequest {

    @NotNull(message = "금액을 입력해주세요.")
    @DecimalMin(value = "1", message = "최소 충전액은 1원입니다.")
    private BigDecimal amount;

    private String description;
}
