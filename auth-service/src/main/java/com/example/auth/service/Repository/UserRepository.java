package com.example.auth.service.Repository;

import com.example.auth.service.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User,Integer> {
    Optional<User> findByUsername(String username);
}
