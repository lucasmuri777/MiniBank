package com.example.minibank.account.service;

import com.example.minibank.account.dto.CreateAccountRequestDTO;
import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.entity.Account;
import com.example.minibank.account.mapper.AccountMapper;
import com.example.minibank.account.repository.AccountRepository;

import com.example.minibank.shared.exception.BusinessException;
import com.example.minibank.transaction.service.TransactionService;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final AccountMapper accountMapper;

    //Criar uma nova conta
    public AccountResponseDTO createAccount(CreateAccountRequestDTO dto){
        log.info(
                "Criação de conta iniciada: createAccountRequestDTO={}", dto);
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .type(dto.getType())
                .active(true)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info(
                "Criação da conta realizado com sucesso: createAccountRequestDTO={}",
                dto);
        return accountMapper.toResponse(savedAccount);
    }

    //Busca pelo id
    public AccountResponseDTO getAccountById(UUID id){
        return accountMapper.toResponse(findById(id));
    }

    //Busca todos
    public List<AccountResponseDTO> getAllAccounts(){
        return accountMapper.toResponseList(accountRepository.findAll());
    }

    //Atualiza
    @Transactional
    public AccountResponseDTO updateAccount(UUID id, CreateAccountRequestDTO dto){
        log.info(
                "Update account iniciado: accountId={}, createAccountRequestDTO={}",
                id, dto);
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Usuário não encontrado"));
        Account account = findByIdForUpdate(id);
        account.setType(dto.getType());
        account.setUser(user);

        Account accountSaved = accountRepository.save(account);
        log.info(
                "Update account realizado com sucesso: accountId={}, createAccountRequestDTO={}",
            id, dto);
        return accountMapper.toResponse(accountSaved);
    }
    //Exclui
    public void deleteAccount(UUID id){
        log.info("Delete account iniciado: accountId={}", id);
        if(!accountRepository.existsById(id)){
            throw new BusinessException(HttpStatus.NOT_FOUND,"Account não encontrado");
        }
        accountRepository.deleteById(id);
        log.info("Account deletada: accountId={}", id);
    }

    //Deposit deposito em conta
    @Transactional
    public AccountResponseDTO deposit(UUID accountId, BigDecimal amount){
        log.info("Depósito iniciado: accountId={}, amount={}", accountId, amount);
        Account account = findByIdForUpdate(accountId);
        validateActiveAccount(account);

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Valor do depósido menor ou igual a 0: accountId={}, amount={}", accountId, amount);
            throw new BusinessException(HttpStatus.CONFLICT,"Valor inválido");
        }

        BigDecimal lastBalance = account.getBalance();
        BigDecimal newBalance = lastBalance.add(amount);

        account.setBalance(newBalance);

        Account accountSaved = accountRepository.save(account);
        transactionService.saveDeposit(accountId, amount);

        log.info("Depósito realizado com sucesso: accountId={}, amount={}", accountId, amount);
        return accountMapper.toResponse(accountSaved);
    }

    //Withdraw sacar saldo
    @Transactional
    public AccountResponseDTO withdraw(UUID accountId, BigDecimal amount){
        log.info("Saque iniciado: accountId={}, amount={}", accountId, amount);
        Account account = findByIdForUpdate(accountId);
        validateActiveAccount(account);
        BigDecimal lastBalance = account.getBalance();
        if(lastBalance.compareTo(amount) == -1){
            log.warn("Valor do saque maior do que tem na conta: accountId={}, amount={}", accountId, amount);
            throw new BusinessException(HttpStatus.BAD_REQUEST,"Valor que deseja sacar é maior do que tem em conta");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Valo do saque menor ou igual a 0: accountId={}, amount={}", accountId, amount);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Valor inválido");
        }
        BigDecimal newBalance = lastBalance.subtract(amount);
        account.setBalance(newBalance);

        Account accountSaved = accountRepository.save(account);
        transactionService.saveWithdraw(accountId, amount);

        log.info("Saque realizado com sucesso: accountId={}, amount={}", accountId, amount);
        return accountMapper.toResponse(accountSaved);
    }

    //Transfer transferir entre contas
    @Transactional
    public AccountResponseDTO transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount){
        log.info("Transferencia iniciada: fromAccountId={}, toAccountId={}, amount={}", fromAccountId, toAccountId, amount);
        if(fromAccountId.equals(toAccountId)){
            log.warn(
                    "Não pode transferir para a mesma conta: fromAccountId={}, toAccountId={}, amount={}",
                    fromAccountId, toAccountId, amount);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Transferência inválida");
        }
        Account fromAccount = findByIdForUpdate(fromAccountId);
        Account toAccount = findByIdForUpdate(toAccountId);

        validateActiveAccount(fromAccount);
        validateActiveAccount(toAccount);

        BigDecimal fromAccountBalance = fromAccount.getBalance();
        BigDecimal toAccountBalance = toAccount.getBalance();

        if(fromAccountBalance.compareTo(amount) < 0){
            log.warn("Valor da transferência maior do que salfo em conta: fromAccountId={}, toAccountId={}, amount={}",
                    fromAccountId, toAccountId, amount);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Valor que deseja transferir é maior do que tem em conta");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            log.warn("Valor da transferência menos ou igual a 0: fromAccountId={}, toAccountId={}, amount={}",
                    fromAccountId, toAccountId, amount);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Valor inválido");
        }

        BigDecimal newFromAccountBalance = fromAccountBalance.subtract(amount);
        BigDecimal newToAccountBalance = toAccountBalance.add(amount);

        fromAccount.setBalance(newFromAccountBalance);
        toAccount.setBalance(newToAccountBalance);

        Account fromAccountSaved = accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        transactionService.saveTransfer(fromAccountId, toAccountId, amount);

        log.info("Transferência realizada com sucesso: fromAccountId={}, toAccountId={}, amount={}",
                fromAccountId, toAccountId, amount);
        return accountMapper.toResponse(fromAccountSaved);
    }

    //BlockAccount bloquear conta
    public AccountResponseDTO blockAccount(UUID id){
        log.info("Bloqueio de conta iniciado: accountId={}", id);

        Account account = findByIdForUpdate(id);
        account.setActive(false);
        
        log.info("Conta bloqueada: accountId={}", id);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    public AccountResponseDTO getByAccountNumber(String accountNumber){
        return accountMapper.toResponse(accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND, "Account not found")));
    }

    public List<AccountResponseDTO> getAccountsByUser(UUID userId){
        List<Account> accounts = accountRepository.findAllByUserId(userId);

        return accounts.stream().map(accountMapper::toResponse).toList();
    }

    private Account findByIdForUpdate(UUID id){
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND, "Account nor found"));
    }

    private Account findById(UUID id){
        return accountRepository.findById(id)
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND, "Account not found"));
    }
    private String generateAccountNumber() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }


    private void validateActiveAccount(Account account){
        if(!account.isActive()){
            throw new BusinessException(HttpStatus.FORBIDDEN,"Conta bloqueada");
        }
    }
}
