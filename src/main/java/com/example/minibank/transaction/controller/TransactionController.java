package com.example.minibank.transaction.controller;

import com.example.minibank.account.service.AccountService;
import com.example.minibank.transaction.entity.Transaction;
import com.example.minibank.transaction.entity.TransactionType;
import com.example.minibank.transaction.service.TransactionService;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping("/account/{accountId}")
    public List<Transaction> getStatement(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type,
            Authentication authentication){

        UUID userId = accountService.getAccountById(accountId).getUserId();
        isUserReq(authentication, userId);

        return transactionService.getStatement(accountId, type);
    }

    //Verifica se o user que está logado está alterando accounts dele e nao de outro user
    private void isUserReq(Authentication authentication, UUID userId){
        User user = (User) authentication.getPrincipal();
        if(user.getRole() == Role.ADMIN){
            return;
        }
        if(!user.getId().equals(userId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User não autorizado"
            );
        }
    }
}
