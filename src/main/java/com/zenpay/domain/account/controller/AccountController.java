package com.zenpay.domain.account.controller;

import com.zenpay.domain.account.dto.AccountResponse;
import com.zenpay.domain.account.dto.TransferRequest;
import com.zenpay.domain.account.service.AccountService;
import com.zenpay.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> createAccount(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok("계좌가 개설되었습니다.", accountService.createAccount(userId));
    }

    @GetMapping("/accounts/me")
    public ApiResponse<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(accountService.getMyAccounts(userId));
    }

    @PostMapping("/transfers")
    public ApiResponse<Void> transfer(@AuthenticationPrincipal Long userId,
                                      @Valid @RequestBody TransferRequest request) {
        accountService.transfer(userId, request);
        return ApiResponse.ok("송금이 완료되었습니다.", null);
    }
}