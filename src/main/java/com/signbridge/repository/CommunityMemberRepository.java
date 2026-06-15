package com.signbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.signbridge.entity.CommunityMember;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {

        // 이메일로 내 프로필 목록 조회 (여러 개 가능)
        List<CommunityMember> findByUserEmail(String userEmail);

        // chatId 중복 확인
        boolean existsByChatId(String chatId);

        // 같은 이메일 + 같은 역할 중복 확인
        boolean existsByUserEmailAndRole(String userEmail, String role);

        // 공개 프로필 전체 조회 (최신순)
        List<CommunityMember> findByPublicProfileTrueOrderByCreatedAtDesc();

        // 역할 필터
        List<CommunityMember> findByRoleAndPublicProfileTrueOrderByCreatedAtDesc(String role);

        // 지역 필터
        List<CommunityMember> findByRegionAndPublicProfileTrueOrderByCreatedAtDesc(String region);

        // 역할 + 지역 필터
        List<CommunityMember> findByRoleAndRegionAndPublicProfileTrueOrderByCreatedAtDesc(
                        String role, String region);

        // 키워드 검색
        @Query("SELECT m FROM CommunityMember m WHERE m.publicProfile = true AND " +
                        "(m.name LIKE %:keyword% OR m.intro LIKE %:keyword% OR m.speciality LIKE %:keyword%)")
        List<CommunityMember> searchByKeyword(String keyword);
}