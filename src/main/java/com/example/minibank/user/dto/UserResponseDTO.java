package com.example.minibank.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
/*
    DTO de SAÍDA.

    Representa o que a API devolve.
*/

@Getter
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;

    private String name;

    private String email;
}
