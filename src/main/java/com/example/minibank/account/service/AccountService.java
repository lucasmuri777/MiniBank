package com.example.minibank.account.service;

import com.example.minibank.account.dto.CreateAccountRequestDTO;
import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.entity.Account;
import com.example.minibank.account.repository.AccountRepository;

import com.example.minibank.shared.exception.ResourceNotFoundException;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    //Criar uma nova conta
    public AccountResponseDTO createAccount(CreateAccountRequestDTO dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("Usuário não encontrado"));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .type(dto.getType())
                .active(true)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);

        return responseDTO(savedAccount);
    }

    //Busca pelo id
    public AccountResponseDTO getAccountById(UUID id){
        return responseDTO(findById(id));
    }

    //Busca todos
    public List<AccountResponseDTO> getAllAccounts(){
        return accountRepository.findAll()
                .stream()
                .map(this::responseDTO)
                .toList();
    }

    //Atualiza
    public AccountResponseDTO updateAccount(UUID id, CreateAccountRequestDTO dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("Usuário não encontrado"));
        Account account = findById(id);
        account.setType(dto.getType());
        account.setUser(user);

        Account accountSaved = accountRepository.save(account);

        return responseDTO(accountSaved);
    }
    //Exclui
    public void deleteAccount(UUID id){
        if(!accountRepository.existsById(id)){
            throw new ResourceNotFoundException("Account não encontrado");
        }
        accountRepository.deleteById(id);
    }

    //Deposit deposito em conta
    public AccountResponseDTO deposit(UUID accountId, BigDecimal amount){
        Account account = findById(accountId);
        validateActiveAccount(account);
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new ResourceNotFoundException("Valor inválido");
        }

        BigDecimal lastBalance = account.getBalance();
        BigDecimal newBalance = lastBalance.add(amount);

        account.setBalance(newBalance);

        Account accountSaved = accountRepository.save(account);

        return responseDTO(accountSaved);
    }

    //Withdraw sacar saldo
    public AccountResponseDTO withdraw(UUID accountId, BigDecimal amount){
        Account account = findById(accountId);
        validateActiveAccount(account);
        BigDecimal lastBalance = account.getBalance();
        if(lastBalance.compareTo(amount) == -1){
            throw new ResourceNotFoundException("Valor que deseja sacar é maior do que tem em conta");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new ResourceNotFoundException("Valor inválido");
        }
        BigDecimal newBalance = lastBalance.subtract(amount);
        account.setBalance(newBalance);

        Account accountSaved = accountRepository.save(account);

        return responseDTO(accountSaved);
    }

    //Transfer transferir entre contas
    @Transactional
    public AccountResponseDTO transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount){
        if(fromAccountId.equals(toAccountId)){
            throw new ResourceNotFoundException("Transferência inválida");
        }
        Account fromAccount = findById(fromAccountId);
        Account toAccount = findById(toAccountId);

        validateActiveAccount(fromAccount);
        validateActiveAccount(toAccount);

        BigDecimal fromAccountBalance = fromAccount.getBalance();
        BigDecimal toAccountBalance = toAccount.getBalance();

        if(fromAccountBalance.compareTo(amount) < 0){
            throw new ResourceNotFoundException("Valor que deseja transferir é maior do que tem em conta");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new ResourceNotFoundException("Valor inválido");
        }

        BigDecimal newFromAccountBalance = fromAccountBalance.subtract(amount);
        BigDecimal newToAccountBalance = toAccountBalance.add(amount);

        fromAccount.setBalance(newFromAccountBalance);
        toAccount.setBalance(newToAccountBalance);

        Account fromAccountSaved = accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return responseDTO(fromAccountSaved);
    }

    //BlockAccount bloquear conta
    public AccountResponseDTO blockAccount(UUID id){
        Account account = findById(id);
        account.setActive(false);

        Account accountSaved = accountRepository.save(account);

        return responseDTO(accountSaved);
    }

    public AccountResponseDTO getByAccountNumber(String accountNumber){
        return responseDTO(accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found")));
    }

    public List<AccountResponseDTO> getAccountsByUser(UUID userId){
        List<Account> accounts = accountRepository.findAllByUserId(userId);

        return accounts.stream().map(this::responseDTO).toList();
    }

    private Account findById(UUID id){
        return accountRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Account not found"));
    }
    private String generateAccountNumber() {

        return String.valueOf(
                System.currentTimeMillis()
        );
    }

    private AccountResponseDTO responseDTO(Account account){
        return new AccountResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getType(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUser().getId()
        );
    }

    private void validateActiveAccount(Account account){
        if(!account.isActive()){
            throw new ResourceNotFoundException("Conta bloqueada");
        }
    }
}
