package com.signbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.signbridge.entity.ImmigrationCase;

public interface ImmigrationCaseRepository extends JpaRepository<ImmigrationCase, String> {

    // 기관 이메일로 해당 기관의 케이스 전체 조회
    List<ImmigrationCase> findByUserEmailOrderByCaseDateDesc(String userEmail);
}