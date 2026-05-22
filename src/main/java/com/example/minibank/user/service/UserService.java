package com.example.minibank.user.service;

import com.example.minibank.shared.exception.ResourceNotFoundException;

import com.example.minibank.user.dto.CreateUserRequestDTO;
import com.example.minibank.user.dto.UserResponseDTO;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import com.example.minibank.user.repository.UserRepository;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    //Cria um user novo
    public UserResponseDTO createUser(CreateUserRequestDTO dto){
        if(userRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new ResourceNotFoundException("Email já cadastrado");
        }
        //Converte o DTO para Entity
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .build();

        //Salva no postgresql
        User savedUser = userRepository.save(user);

        //coverte Entity -> DTO
        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    //Busca user pelo ID
    public UserResponseDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario não encontrado"));

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    //Atualiza user pelo id
    public UserResponseDTO updateUser(UUID id, CreateUserRequestDTO dto){
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Usuário não encontrado"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User updateUser = userRepository.save(user);

        return new UserResponseDTO(
                updateUser.getId(),
                updateUser.getName(),
                updateUser.getEmail()
        );
    }

    //Pega todos os users
    public List<UserResponseDTO> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user->new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                )).toList();
    }

    //Deletar por id
    public void deleteUserById(UUID id){
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

}
