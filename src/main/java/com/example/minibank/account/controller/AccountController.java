package com.example.minibank.account.controller;

import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.dto.AmountRequestDTO;
import com.example.minibank.account.dto.CreateAccountRequestDTO;
import com.example.minibank.account.dto.TransferRequestDTO;
import com.example.minibank.account.service.AccountService;

import com.example.minibank.common.response.ApiResponse;
import com.example.minibank.security.AuthorizationService;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final AuthorizationService authorizationService;

    //Criar conta
    @PostMapping
    public ApiResponse<AccountResponseDTO> createAccount(
            @Valid @RequestBody CreateAccountRequestDTO account,
            Authentication authentication){

        authorizationService.ensureUserOwnsResource(authentication, account.getUserId());
        return ApiResponse.success(
                "Conta criada com sucesso",
                accountService.createAccount(account));
    }

    //Buscar por id
    @GetMapping("/{id}")
    public ApiResponse<AccountResponseDTO> getAccountById(
            @PathVariable UUID id,
            Authentication authentication){

        AccountResponseDTO account = accountService.getAccountById(id);

        authorizationService.ensureUserOwnsResource(authentication,account.getUserId());

        return ApiResponse.success("Conta encontrada", account);
    }

    //Buscar todas as contas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AccountResponseDTO>> getAllAccounts(){
        return ApiResponse.success("Lista retornada", accountService.getAllAccounts());
    }

    //Atualizar account (type e userId (USER))
    @PutMapping("/{id}")
    public ApiResponse<AccountResponseDTO> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAccountRequestDTO accountUpdate,
            Authentication authentication){

        AccountResponseDTO account = accountService.getAccountById(id);
        authorizationService.ensureUserOwnsResource(authentication, account.getUserId());

        return ApiResponse.success(
                "Conta alterada com sucesso",
                accountService.updateAccount(id, accountUpdate));
    }

    //Deletar account
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAccount(@PathVariable UUID id){
        accountService.deleteAccount(id);
        return ApiResponse.success("Account excluída com sucesso!", null);
    }

    //Deposit route
    @PostMapping("/{accountId}/deposit")
    public ApiResponse<AccountResponseDTO> deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequestDTO dto,
            Authentication authentication){

        authorizationService.ensureUserOwnsResource(authentication, accountService.getAccountById(accountId).getUserId());

        return ApiResponse.success(
                "Depósito realizado com sucesso!",
                accountService.deposit(accountId,  dto.getAmount())
        );
    }

    //WithDraw Sacar saldo
    @PostMapping("/{accountId}/withdraw")
    public ApiResponse<AccountResponseDTO> withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequestDTO dto,
            Authentication authentication){
        UUID userId = accountService.getAccountById(accountId).getUserId();
        authorizationService.ensureUserOwnsResource(authentication, userId);

        return ApiResponse.success(
                "Saque realizado com sucesso",
                accountService.withdraw(accountId, dto.getAmount())
        );
    }

    //Transfer
    @PostMapping("/transfer")
    public ApiResponse<AccountResponseDTO> transfer(
            @Valid @RequestBody TransferRequestDTO dto,
            Authentication authentication){
        UUID userId = accountService.getAccountById(dto.getFromAccountId()).getUserId();
        authorizationService.ensureUserOwnsResource(authentication, userId);

        return ApiResponse.success(
                "Transferencia realizada com sucesso",
                accountService.transfer(dto.getFromAccountId(), dto.getToAccountId(), dto.getAmount())
        );
    }

    //Bloquear conta
    @PatchMapping("/{accountId}/block")
    public ApiResponse<AccountResponseDTO> blockAccount(
            @PathVariable UUID accountId,
            Authentication authentication){

        UUID userId = accountService.getAccountById(accountId).getUserId();
        authorizationService.ensureUserOwnsResource(authentication, userId);

        return ApiResponse.success(
                "Conta bloqueada com sucesso!",
                accountService.blockAccount(accountId)
        );
    }

    //getByAccountNumber pegar account por number
    @GetMapping("/number/{accountNumber}")
    public ApiResponse<AccountResponseDTO> getByAccountNumber(
            @PathVariable String accountNumber,
            Authentication authentication){

        AccountResponseDTO account = accountService.getByAccountNumber(accountNumber);

        authorizationService.ensureUserOwnsResource(authentication, account.getUserId());

        return ApiResponse.success(
                "Conta encontrada pelo número",
                account);
    }

    //getAccountsByUser pegar todas as contas de um usuario pelo userId
    @GetMapping("/user/{userId}")
    public ApiResponse<List<AccountResponseDTO>> getAccountsByUserId(
            @PathVariable UUID userId,
            Authentication authentication){

        authorizationService.ensureUserOwnsResource(authentication, userId);

        return ApiResponse.success(
                "Contas achadas pelo id do usuario",
                accountService.getAccountsByUser(userId)
        );
    }

}
