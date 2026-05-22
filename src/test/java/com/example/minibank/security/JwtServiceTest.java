package com.example.minibank.security;

import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // injeta as propriedades sem precisar subir o Spring
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        user = User.builder()
                .id(UUID.randomUUID())
                .email("lucas@email.com")
                .role(Role.USER)
                .build();
    }

    // ✅ TESTE 1 - gera token e extrai username
    @Test
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("lucas@email.com", jwtService.extractUsername(token));
    }

    // ✅ TESTE 2 - token válido para o usuário correto
    @Test
    void shouldValidateTokenForCorrectUser() {
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    // ✅ TESTE 3 - token inválido para outro usuário
    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken(user);

        User outroUser = User.builder()
                .id(UUID.randomUUID())
                .email("outro@email.com")
                .role(Role.USER)
                .build();

        assertFalse(jwtService.isTokenValid(token, outroUser));
    }

    // ✅ TESTE 4 - refresh token diferente do access token
    @Test
    void shouldGenerateDifferentRefreshToken() {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertNotEquals(accessToken, refreshToken);
    }
}