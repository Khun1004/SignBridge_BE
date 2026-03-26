package com.signbridge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.signbridge.entity.SignHistory;

@Repository
public interface SignHistoryRepository extends JpaRepository<SignHistory, Long> {
    // 기본적인 CRUD 메소드가 자동으로 생성됩니다.
}