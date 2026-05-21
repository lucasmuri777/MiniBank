package com.example.minibank.account.dto;

import com.example.minibank.account.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
/*
    DTO de SAÍDA.

    Representa o que a API devolve.
*/
@Getter
@AllArgsConstructor
public class AccountResponseDTO {

    private UUID id;

    private String accountNumber;

    private BigDecimal balance;

    private AccountType type;

    private boolean active;

    private LocalDateTime createdAt;

    private UUID userId;
}
