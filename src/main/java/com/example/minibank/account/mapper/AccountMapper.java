package com.example.minibank.account.mapper;

import com.example.minibank.account.dto.AccountResponseDTO;
import com.example.minibank.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring") //Spring injeta como @Component
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")//Campo diferente
    AccountResponseDTO toResponse(Account account);

    List<AccountResponseDTO> toResponseList(List<Account> accounts);
}
