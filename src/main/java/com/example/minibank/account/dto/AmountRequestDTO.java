package com.example.minibank.account.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class AmountRequestDTO {
    @NotNull
    @Positive
    private BigDecimal amount;
}
