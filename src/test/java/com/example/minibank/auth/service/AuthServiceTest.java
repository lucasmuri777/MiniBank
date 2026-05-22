package com.example.minibank.auth.service;

import com.example.minibank.auth.dto.AuthResponseDTO;
import com.example.minibank.auth.dto.LoginRequestDTO;
import com.example.minibank.auth.dto.RegisterRequestDTO;
import com.example.minibank.security.JwtService;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import com.example.minibank.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ✅ TESTE 1 - register com sucesso
    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Lucas", "lucas@email.com", "123");

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("senhaHash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponseDTO response = authService.register(dto);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRepository).save(any(User.class));
    }

    // ✅ TESTE 2 - register com email já existente
    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Lucas", "lucas@email.com", "123");

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    // ✅ TESTE 3 - login com sucesso
    @Test
    void shouldLoginSuccessfully() {
        LoginRequestDTO dto = new LoginRequestDTO("lucas@email.com", "123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("lucas@email.com")
                .password("senhaHash")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // ✅ TESTE 4 - login com credenciais erradas
    @Test
    void shouldThrowWhenCredentialsAreWrong() {
        LoginRequestDTO dto = new LoginRequestDTO("lucas@email.com", "senhaErrada");

        doThrow(new BadCredentialsException("Credenciais inválidas"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
        verify(userRepository, never()).findByEmail(any());
    }

    // ✅ TESTE 5 - refresh token válido
    @Test
    void shouldRefreshTokenSuccessfully() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("lucas@email.com")
                .role(Role.USER)
                .build();

        when(jwtService.extractUsername("refresh-token")).thenReturn("lucas@email.com");
        when(userRepository.findByEmail("lucas@email.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh-token", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("novo-access-token");

        AuthResponseDTO response = authService.refreshToken("refresh-token");

        assertEquals("novo-access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    // ✅ TESTE 6 - refresh token inválido
    @Test
    void shouldThrowWhenRefreshTokenIsInvalid() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("lucas@email.com")
                .role(Role.USER)
                .build();

        when(jwtService.extractUsername("token-expirado")).thenReturn("lucas@email.com");
        when(userRepository.findByEmail("lucas@email.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("token-expirado", user)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.refreshToken("token-expirado"));
    }
}