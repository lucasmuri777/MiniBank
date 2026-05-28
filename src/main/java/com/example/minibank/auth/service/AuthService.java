package com.example.minibank.auth.service;

import com.example.minibank.auth.dto.*;
import com.example.minibank.security.JwtService;
import com.example.minibank.shared.exception.BusinessException;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import com.example.minibank.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO request){
        log.info("Tentativa de login: email={}", request.email());
        //1 Autentica (lança exceção se credenciais errada)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        //2 Busca o usuário
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND,"Usuario não encontrado"));

        //3 Gera os tokens
        String acessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.info("Login realizado com sucesso: email ={}", request.email());
        return new AuthResponseDTO(acessToken, refreshToken);
    }

    public AuthResponseDTO register(RegisterRequestDTO request){
        log.info("Tentativa de registro de novo usuário: email={}", request.email());
        //1 verifica se email já existe

        if(userRepository.findByEmail(request.email()).isPresent()){
            log.warn("Registro recusado, email já existe: email={}", request.email());
            throw new BusinessException(HttpStatus.CONFLICT,"Email já cadastrado");
        }

        //2 Cria o usuario com senha criptografada
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        //3 já retorna os tokens (usuario já entra logafo)
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Registro realizado com sucesso: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public AuthResponseDTO refreshToken(String refreshToken){
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow();

        if(jwtService.isTokenValid(refreshToken, user)){
            String newAccessToken = jwtService.generateToken(user);
            return new AuthResponseDTO(newAccessToken, refreshToken);
        }
        throw new BusinessException(HttpStatus.UNAUTHORIZED,"Refresh token inválido");
    }

}
