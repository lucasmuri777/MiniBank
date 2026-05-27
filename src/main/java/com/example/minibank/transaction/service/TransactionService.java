package com.example.minibank.transaction.service;

import com.example.minibank.transaction.entity.Transaction;
import com.example.minibank.transaction.entity.TransactionType;
import com.example.minibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    //Deposito
    public void saveDeposit(UUID accountId, BigDecimal amount){
        Transaction transaction = Transaction.builder()
                .toAccountId(accountId)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    public void saveWithdraw(UUID accountId, BigDecimal amount){
        Transaction transaction = Transaction.builder()
                .fromAccountId(accountId)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    public void saveTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount){
        Transaction transaction = Transaction.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    public List<Transaction> getStatement(UUID accountId, TransactionType type){
        List<Transaction> transactions = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);

        if(type == null){
            return transactions;
        }
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

}
