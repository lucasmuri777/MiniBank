package com.example.minibank.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDTO {
    @NotNull
    private UUID fromAccountId;
    @NotNull
    private UUID toAccountId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
