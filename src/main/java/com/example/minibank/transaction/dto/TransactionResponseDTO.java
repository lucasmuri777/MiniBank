package com.example.minibank.transaction.dto;

import com.example.minibank.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransactionResponseDTO {

    private UUID id;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime createdAt;
}
