package com.example.minibank.user.service;

import com.example.minibank.user.dto.CreateUserRequestDTO;
import com.example.minibank.user.dto.UserResponseDTO;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.repository.UserRepository;
import com.example.minibank.shared.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    //Teste 1 - criar user
    @Test
    void shouldCreateUserSuccessfully(){
        //Arrange preparar Dados
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setName("Lucas");
        dto.setEmail("lucas@email.com");
        dto.setPassword("123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        //ACT (executar)
        UserResponseDTO response = userService.createUser(dto);

        //Assert(verificar)
        assertNotNull(response);
        assertEquals("Lucas", response.getName());
        assertEquals("lucas@email.com", response.getEmail());
    }

    // 🧪 TESTE 2 - buscar user por id
    @Test
    void shouldReturnUserById() {

        UUID id = UUID.randomUUID();

        User user = User.builder()
                .id(id)
                .name("Lucas")
                .email("lucas@email.com")
                .password("123")
                .build();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserById(id);

        assertEquals(id, response.getId());
        assertEquals("Lucas", response.getName());
    }

    // 🧪 TESTE 3 - user não existe (erro)
    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(id);
        });
    }

    //TESTE 4 - GET ALL USERS
    @Test
    void shouldReturnAllUsers(){
        List<User> users = List.of(
                User.builder()
                        .id(UUID.randomUUID())
                        .name("Lucas")
                        .email("lucas@email.com")
                        .password("123")
                        .build(),

                User.builder()
                        .id(UUID.randomUUID())
                        .name("Ana")
                        .email("ana@email.com")
                        .password("123")
                        .build()
        );

        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseDTO> response = userService.getAllUsers();

        assertEquals(2, response.size());
    }

    //Teste 5 - Update user
    @Test
    void shouldUpdateUserSuccessfully(){
        UUID id = UUID.randomUUID();

        User existingUser = User.builder()
                .id(id)
                .name("Old name")
                .email("old@email.com")
                .password("123")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setName("New name");
        dto.setEmail("new@email.com");
        dto.setPassword("123");

        UserResponseDTO response = userService.updateUser(id, dto);

        assertEquals("New name", response.getName());
        assertEquals("new@email.com", response.getEmail());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id)).thenReturn(true);

        doNothing().when(userRepository).deleteById(id);

        assertDoesNotThrow(() -> userService.deleteUserById(id));

        verify(userRepository, times(1)).deleteById(id);
    }
}
