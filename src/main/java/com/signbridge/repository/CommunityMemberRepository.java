package com.signbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.signbridge.entity.CommunityMember;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {

    // 이메일로 내 프로필 조회
    Optional<CommunityMember> findByUserEmail(String userEmail);

    // 공개 프로필 전체 조회 (최신순)
    List<CommunityMember> findByPublicProfileTrueOrderByCreatedAtDesc();

    // 역할 필터
    List<CommunityMember> findByRoleAndPublicProfileTrueOrderByCreatedAtDesc(String role);

    // 지역 필터
    List<CommunityMember> findByRegionAndPublicProfileTrueOrderByCreatedAtDesc(String region);

    // 역할 + 지역 필터
    List<CommunityMember> findByRoleAndRegionAndPublicProfileTrueOrderByCreatedAtDesc(
            String role, String region);

    // 키워드 검색 (이름, 자기소개, 전문분야)
    @Query("SELECT m FROM CommunityMember m WHERE m.publicProfile = true AND " +
            "(m.name LIKE %:keyword% OR m.intro LIKE %:keyword% OR m.speciality LIKE %:keyword%)")
    List<CommunityMember> searchByKeyword(String keyword);
}