package com.example.minibank.user.mapper;

import com.example.minibank.user.dto.UserResponseDTO;
import com.example.minibank.user.entity.User;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponse(User user);

    List<UserResponseDTO> toResponseList(List<User> users);
}
