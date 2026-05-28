package com.example.minibank.security;

import com.example.minibank.shared.exception.BusinessException;
import com.example.minibank.user.entity.User;
import com.example.minibank.user.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {
    public void ensureUserOwnsResource(Authentication authentication, UUID resourceUserId){
        ensureUserOwnsResource(authentication, resourceUserId, "Acesso negado");
    }

    public void ensureUserOwnsResource(
            Authentication authentication,
            UUID resourceUserId,
            String message
    ){
        User user = (User) authentication.getPrincipal();
        if(user.getRole() == Role.ADMIN){
            return;
        }
        if(!user.getId().equals(resourceUserId)){
            throw new BusinessException(HttpStatus.FORBIDDEN, message);
        }
    }

}
