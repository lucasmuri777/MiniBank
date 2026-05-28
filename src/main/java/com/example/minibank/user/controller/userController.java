package com.example.minibank.user.controller;

import com.example.minibank.common.response.ApiResponse;
import com.example.minibank.security.AuthorizationService;
import com.example.minibank.shared.exception.BusinessException;
import com.example.minibank.user.dto.CreateUserRequestDTO;
import com.example.minibank.user.dto.UserResponseDTO;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class userController {
    private final UserService userService;
    private final AuthorizationService authorizationService;

    //GET /users/{id}
    @GetMapping("/{id}")
    public ApiResponse<UserResponseDTO> getUserById(@PathVariable UUID id){

        return ApiResponse.success(
                "Conta encontrada",
                userService.getUserById(id)
        );
    }

    //GET /users
    @GetMapping
    public ApiResponse<List<UserResponseDTO>> getAllUsers(){
        return ApiResponse.success("Usuarios exibidos", userService.getAllUsers());
    }

    //PUT /users/{id}
    @PutMapping("/{id}")
    public ApiResponse<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUserRequestDTO dto,
            Authentication authentication){

        authorizationService.ensureUserOwnsResource(authentication, id,"Você não pode editar outro usuário");

        return ApiResponse.success(
                "Usuário atualizado com sucesso",
                userService.updateUser(id, dto));
    }

    //DELETE /users/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id, Authentication authentication){
        authorizationService.ensureUserOwnsResource(authentication, id, "Você não pode excluir outro usuário");

        userService.deleteUserById(id);

        return ApiResponse.success("Usuário excluído com sucesso", null);
    }

}
