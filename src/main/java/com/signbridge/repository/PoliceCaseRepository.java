package com.signbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.signbridge.entity.PoliceCase;

public interface PoliceCaseRepository extends JpaRepository<PoliceCase, String> {
    List<PoliceCase> findByUserEmailOrderByCaseDateDesc(String userEmail);
}