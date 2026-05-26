package com.example.minibank.account.controller;

import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.dto.AmountRequestDTO;
import com.example.minibank.account.dto.CreateAccountRequestDTO;
import com.example.minibank.account.dto.TransferRequestDTO;
import com.example.minibank.account.entity.Account;
import com.example.minibank.account.service.AccountService;

import com.example.minibank.shared.exception.ResourceNotFoundException;
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

    //Criar conta
    @PostMapping
    public AccountResponseDTO createAccount(
            @Valid @RequestBody CreateAccountRequestDTO account,
            Authentication authentication){

        isUserReq(authentication, account.getUserId());
        return accountService.createAccount(account);
    }

    //Buscar por id
    @GetMapping("/{id}")
    public AccountResponseDTO getAccountById(
            @PathVariable UUID id,
            Authentication authentication){

        AccountResponseDTO account = accountService.getAccountById(id);

        isUserReq(authentication,account.getUserId());

        return account;
    }

    //Buscar todas as contas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountResponseDTO> getAllAccounts(){
        return accountService.getAllAccounts();
    }

    //Atualizar account (type e userId (USER))
    @PutMapping("/{id}")
    public AccountResponseDTO updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAccountRequestDTO accountUpdate,
            Authentication authentication){

        AccountResponseDTO account = accountService.getAccountById(id);
        isUserReq(authentication, account.getUserId());

        return accountService.updateAccount(id, accountUpdate);
    }

    //Deletar account
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAccount(@PathVariable UUID id){
        accountService.deleteAccount(id);
    }

    //Deposit route
    @PostMapping("/{accountId}/deposit")
    public AccountResponseDTO deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequestDTO dto,
            Authentication authentication){

        isUserReq(authentication, accountService.getAccountById(accountId).getUserId());

        return accountService.deposit(accountId, dto.getAmount());
    }

    //WithDraw Sacar saldo
    @PostMapping("/{accountId}/withdraw")
    public AccountResponseDTO withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody AmountRequestDTO dto,
            Authentication authentication){
        UUID userId = accountService.getAccountById(accountId).getUserId();
        isUserReq(authentication, userId);
        return accountService.withdraw(accountId, dto.getAmount());
    }

    //Transfer
    @PostMapping("/transfer")
    public AccountResponseDTO transfer(
            @Valid @RequestBody TransferRequestDTO dto,
            Authentication authentication){
        UUID userId = accountService.getAccountById(dto.getFromAccountId()).getUserId();
        isUserReq(authentication, userId);
        return accountService.transfer(dto.getFromAccountId(), dto.getToAccountId(), dto.getAmount());
    }

    //Bloquear conta
    @PatchMapping("/{accountId}/block")
    public AccountResponseDTO blockAccount(
            @PathVariable UUID accountId,
            Authentication authentication){

        UUID userId = accountService.getAccountById(accountId).getUserId();
        isUserReq(authentication, userId);
        return accountService.blockAccount(accountId);
    }

    //getByAccountNumber pegar account por number
    @GetMapping("/number/{accountNumber}")
    public AccountResponseDTO getByAccountNumber(
            @PathVariable String accountNumber,
            Authentication authentication){

        AccountResponseDTO account = accountService.getByAccountNumber(accountNumber);

        isUserReq(authentication, account.getUserId());

        return account;
    }

    //getAccountsByUser pegar todas as contas de um usuario pelo userId
    @GetMapping("/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUserId(
            @PathVariable UUID userId,
            Authentication authentication){

        isUserReq(authentication, userId);
        return accountService.getAccountsByUser(userId);
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
