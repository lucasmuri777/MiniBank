package com.example.minibank.transaction.service;

import com.example.minibank.transaction.dto.TransactionResponseDTO;
import com.example.minibank.transaction.entity.Transaction;
import com.example.minibank.transaction.entity.TransactionType;
import com.example.minibank.transaction.mapper.TransactionMapper;
import com.example.minibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    //Deposito
    public void saveDeposit(UUID accountId, BigDecimal amount){
        log.info(
                "Transaction de Depósito iniciado: accountId={}, amount={}",
                accountId, amount);

        Transaction transaction = Transaction.builder()
                .toAccountId(accountId)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction de Depósito realizado com sucesso! accountId={}, amount={}",
                accountId, amount);
    }

    public void saveWithdraw(UUID accountId, BigDecimal amount){
        log.info(
                "Transaction de Saque iniciado: accountId={}, amount={}",
                accountId, amount);
        Transaction transaction = Transaction.builder()
                .fromAccountId(accountId)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        log.info("Transaction de Saque realizado com sucesso! accountId={}, amount={}",
                accountId, amount);
    }

    public void saveTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount){
        log.info(
                "Transaction de Transferência iniciado: fromAccountId={}, toAccountId={}, amount={}",
                fromAccountId, toAccountId, amount);

        Transaction transaction = Transaction.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        log.info(
                "Transaction de Transferência realizado com sucesso! fromAccountId={}, toAccountId={}, amount={}",
                fromAccountId, toAccountId, amount);
    }

    public List<TransactionResponseDTO> getStatement(UUID accountId, TransactionType type){
        List<Transaction> transactions = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);

        if(type == null){
            return transactionMapper.toResponseList(transactions);
        }
        List<Transaction> filtered = transactions.stream()
                .filter(t -> t.getType() == type)
                .toList();

        return transactionMapper.toResponseList(filtered);
    }

}
