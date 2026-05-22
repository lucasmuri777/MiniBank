package com.example.minibank.auth.service;

import com.example.minibank.auth.dto.*;
import com.example.minibank.security.JwtService;
import com.example.minibank.shared.exception.ResourceNotFoundException;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import com.example.minibank.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request){
        //1 Autentica (lança exceção se credenciais errada)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        //2 Busca o usuário
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(()-> new ResourceNotFoundException("Usuario não encontrado"));

        //3 Gera os tokens
        String acessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponseDTO(acessToken, refreshToken);
    }

    public AuthResponseDTO register(RegisterRequestDTO request){
        //1 verifica se email já existe
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new RuntimeException("Email já cadastrado");
        }

        //2 Cria o usuario com senha criptografada
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        //3 já retorna os tokens (usuario já entra logafo)
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO refreshToken(String refreshToken){
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow();

        if(jwtService.isTokenValid(refreshToken, user)){
            String newAccessToken = jwtService.generateToken(user);
            return new AuthResponseDTO(newAccessToken, refreshToken);
        }
        throw new RuntimeException("Refresh token inválido");
    }

}
