package com.example.minibank.account.dto;

import com.example.minibank.account.enums.AccountType;

import jakarta.validation.constraints.NotNull;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class CreateAccountRequestDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private AccountType type;
}
