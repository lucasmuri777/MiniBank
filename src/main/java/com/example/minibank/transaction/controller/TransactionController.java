package com.example.minibank.transaction.controller;

import com.example.minibank.account.service.AccountService;
import com.example.minibank.security.AuthorizationService;
import com.example.minibank.shared.exception.BusinessException;
import com.example.minibank.transaction.entity.TransactionType;
import com.example.minibank.transaction.service.TransactionService;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import com.example.minibank.common.response.ApiResponse;
import com.example.minibank.transaction.dto.TransactionResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuthorizationService authorizationService;

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<TransactionResponseDTO>> getStatement(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type,
            Authentication authentication){

        UUID userId = accountService.getAccountById(accountId).getUserId();
        authorizationService.ensureUserOwnsResource(authentication, userId);

        List<TransactionResponseDTO> data =
                transactionService.getStatement(accountId, type);

        return ApiResponse.success("Extrato obtido com sucesso", data);
    }


}
