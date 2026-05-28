package com.example.minibank.transaction.mapper;

import com.example.minibank.transaction.dto.TransactionResponseDTO;
import com.example.minibank.transaction.entity.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponseDTO toResponse(Transaction transaction);

    List<TransactionResponseDTO> toResponseList(List<Transaction> transactions);
}
