package com.example.minibank.auth.controller;

import com.example.minibank.auth.dto.*;
import com.example.minibank.auth.service.AuthService;

import com.example.minibank.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    public final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody LoginRequestDTO request){
        AuthResponseDTO body = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login realizado com sucesso", body)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refresh(@RequestBody String refreshToken){
        AuthResponseDTO body = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.success("Refresh realizado com sucesso", body)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
                @Valid @RequestBody RegisterRequestDTO request){
        AuthResponseDTO data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuário registrado com sucesso", data));
    }

}
