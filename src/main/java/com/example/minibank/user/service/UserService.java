package com.example.minibank.user.service;

import com.example.minibank.shared.exception.BusinessException;
import com.example.minibank.user.dto.CreateUserRequestDTO;
import com.example.minibank.user.dto.UserResponseDTO;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import com.example.minibank.user.mapper.UserMapper;
import com.example.minibank.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    //Cria um user novo
    public UserResponseDTO createUser(CreateUserRequestDTO dto){
        log.info("CreateUser iniciado: email={}", dto.getEmail());

        if(userRepository.findByEmail(dto.getEmail()).isPresent()){
            log.warn("CreateUser error: Email já cadastrado, email={}", dto.getEmail());
            throw new BusinessException(HttpStatus.CONFLICT, "Email já cadastrado");
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

        log.info("CreateUser realizado com sucesso! id={}, email={}", savedUser.getId(), savedUser.getEmail());
        //coverte Entity -> DTO
        return userMapper.toResponse(savedUser);
    }

    //Busca user pelo ID
    public UserResponseDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND, "Usuario não encontrado"));

        return userMapper.toResponse(user);
    }

    //Atualiza user pelo id
    public UserResponseDTO updateUser(UUID userId, CreateUserRequestDTO dto){
        log.info("updateUser iniciado: userId={}, email={}", userId, dto.getEmail());
        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User updateUser = userRepository.save(user);

        log.info("updateUser realizado com sucesso! userId={}, email={}", userId, updateUser.getEmail());
        return userMapper.toResponse(updateUser);
    }

    //Pega todos os users
    public List<UserResponseDTO> getAllUsers(){
        return userMapper.toResponseList(userRepository.findAll());
    }

    //Deletar por id
    public void deleteUserById(UUID id){
        log.info("deleteUserById iniciado: userId={}", id);
        if(!userRepository.existsById(id)){
            log.warn("DeleteUSerById error: User não encontrado! userId={}", id);
            throw new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        userRepository.deleteById(id);
        log.info("delteUserById realizado com sucesso! userId={}", id);
    }

}
