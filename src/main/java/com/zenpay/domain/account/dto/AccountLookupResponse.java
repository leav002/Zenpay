package com.zenpay.domain.account.dto;

import com.zenpay.domain.account.entity.Account;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountLookupResponse {
    private Long id;
    private String accountNumber;

    public static AccountLookupResponse from(Account account) {
        return AccountLookupResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .build();
    }
}
