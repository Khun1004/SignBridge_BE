package com.signbridge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.signbridge.entity.User; // 이 임포트가 정확해야 합니다.

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}