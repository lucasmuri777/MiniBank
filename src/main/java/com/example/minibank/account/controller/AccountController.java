package com.example.minibank.account.controller;

import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.dto.AmountRequestDTO;
import com.example.minibank.account.dto.CreateAccountRequestDTO;
import com.example.minibank.account.dto.TransferRequestDTO;
import com.example.minibank.account.service.AccountService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    //Criar conta
    @PostMapping
    public AccountResponseDTO createAccount(@Valid @RequestBody CreateAccountRequestDTO account){
        return accountService.createAccount(account);
    }

    //Buscar por id
    @GetMapping("/{id}")
    public AccountResponseDTO getAccountById(@PathVariable UUID id){
        return accountService.getAccountById(id);
    }

    //Buscar todas as contas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountResponseDTO> getAllAccounts(){
        return accountService.getAllAccounts();
    }

    //Atualizar account (type e userId (USER))
    @PutMapping("/{id}")
    public AccountResponseDTO updateAccount(@PathVariable UUID id, @Valid @RequestBody CreateAccountRequestDTO accountUpdate){
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
    public AccountResponseDTO deposit(@PathVariable UUID accountId, @Valid @RequestBody AmountRequestDTO dto){
        return accountService.deposit(accountId, dto.getAmount());
    }

    //WithDraw Sacar saldo
    @PostMapping("/{accountId}/withdraw")
    public AccountResponseDTO withdraw(@PathVariable UUID accountId, @Valid @RequestBody AmountRequestDTO dto){
        return accountService.withdraw(accountId, dto.getAmount());
    }

    //Transfer
    @PostMapping("/transfer")
    public AccountResponseDTO transfer(@Valid @RequestBody TransferRequestDTO dto){
        return accountService.transfer(dto.getFromAccountId(), dto.getToAccountId(), dto.getAmount());
    }

    //Bloquear conta
    @PatchMapping("/{accountId}/block")
    public AccountResponseDTO blockAccount(@PathVariable UUID accountId){
        return accountService.blockAccount(accountId);
    }

    //getByAccountNumber pegar account por number
    @GetMapping("/number/{accountNumber}")
    public AccountResponseDTO getByAccountNumber(@PathVariable String accountNumber){
        return accountService.getByAccountNumber(accountNumber);
    }

    //getAccountsByUser pegar todas as contas de um usuario pelo userId
    @GetMapping("/user/{userId}")
    public List<AccountResponseDTO> getAccountsByUserId(@PathVariable UUID userId){
        return accountService.getAccountsByUser(userId);
    }

}
