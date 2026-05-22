package com.example.minibank.user.entity;

import com.example.minibank.user.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password; //será armazenado com BCrypt

    @Enumerated(EnumType.STRING)
    private Role role;

    // ===== Métodos do UserDetails =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        //Transfirma o Role em permissão que o Spring Security entende
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername(){
        return email; //usamos email como "Username"
    }

    @Override
    public boolean isAccountNonExpired(){ return true; }

    @Override
    public boolean isAccountNonLocked(){ return true; }

    @Override
    public boolean isCredentialsNonExpired(){ return true; }

    @Override
    public boolean isEnabled(){ return true; }



}
