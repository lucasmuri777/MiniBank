package com.example.minibank.user.controller;

import com.example.minibank.user.dto.CreateUserRequestDTO;
import com.example.minibank.user.dto.UserResponseDTO;
import com.example.minibank.user.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class userController {
    private final UserService userService;

    //GET /users/{id}
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable UUID id){
        return userService.getUserById(id);
    }

    //GET /users
    @GetMapping
    public List<UserResponseDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    //PUT /users/{id}
    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable UUID id, @Valid @RequestBody CreateUserRequestDTO dto){
        return userService.updateUser(id, dto);
    }

    //DELETE /users/{id}
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id){
        userService.deleteUserById(id);
    }
}
