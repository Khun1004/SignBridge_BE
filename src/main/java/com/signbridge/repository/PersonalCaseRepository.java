package com.signbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.signbridge.entity.PersonalCase;

public interface PersonalCaseRepository extends JpaRepository<PersonalCase, Long> {
    List<PersonalCase> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PersonalCase p WHERE p.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);
}