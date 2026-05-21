package com.example.minibank.user.repository;

import com.example.minibank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Não precisa escrever nada aqui por enquanto!
    // O Spring já te dá: findAll, findById, save, deleteById...
    Optional<User> findByEmail(String email);
}
