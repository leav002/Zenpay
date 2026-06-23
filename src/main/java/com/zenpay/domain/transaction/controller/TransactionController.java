package com.zenpay.domain.transaction.controller;

import com.zenpay.domain.transaction.dto.TransactionResponse;
import com.zenpay.domain.transaction.service.TransactionService;
import com.zenpay.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ApiResponse<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long accountId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.ok(transactionService.getTransactions(userId, accountId, pageable));
    }
}