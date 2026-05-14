package com.signbridge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.MyPageDto;
import com.signbridge.entity.User;
import com.signbridge.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MyPageController {

    private final UserRepository userRepository;

    // ── 프로필 조회 ────────────────────────────────────────────
    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getProfile(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildProfileResponse(userOpt.get()));
    }

    // ── 프로필 수정 (이름, 장애등급, 주사용수어) ──────────────
    @PatchMapping("/profile/{email}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String email,
            @RequestBody Map<String, String> req) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.notFound().build();

        User user = userOpt.get();

        // 이름 — 필수
        String name = req.get("name");
        if (name != null && !name.isBlank()) {
            user.setName(name.trim());
        }
        // 장애 등급 — 선택
        String grade = req.get("disabilityGrade");
        if (grade != null) {
            user.setDisabilityGrade(grade.trim());
        }
        // 주로 사용하는 수어 — 선택
        String sign = req.get("preferredSign");
        if (sign != null) {
            user.setPreferredSign(sign.trim());
        }

        userRepository.save(user);
        return ResponseEntity.ok(buildProfileResponse(user));
    }

    // ── 기관별 케이스 목록 ─────────────────────────────────────
    @GetMapping("/cases/{email}")
    public ResponseEntity<List<MyPageDto.CaseItem>> getCases(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        List<MyPageDto.CaseItem> cases = new ArrayList<>();
        if (user == null)
            return ResponseEntity.ok(cases);

        if ("immigration".equals(user.getOrgType())) {
            cases.add(MyPageDto.CaseItem.builder()
                    .id(1L).title("비자 체류자격 변경").applicant("홍길동").status("검토중").build());
        } else if ("police".equals(user.getOrgType())) {
            cases.add(MyPageDto.CaseItem.builder()
                    .id(2L).title("교통사고 진술조서").applicant("김철수").status("접수완료").build());
        }
        return ResponseEntity.ok(cases);
    }

    // ── 공통: User → 응답 Map ──────────────────────────────────
    private Map<String, String> buildProfileResponse(User user) {
        Map<String, String> res = new HashMap<>();
        res.put("email", safe(user.getEmail()));
        res.put("name", safe(user.getName()));
        res.put("orgType", safe(user.getOrgType()));
        res.put("officeName", safe(user.getOfficeName()));
        res.put("orgCode", safe(user.getOrgCode()));
        res.put("address", safe(user.getAddress()));
        res.put("addressDetail", safe(user.getAddressDetail()));
        res.put("zonecode", safe(user.getZonecode()));
        res.put("disabilityGrade", safe(user.getDisabilityGrade()));
        res.put("preferredSign", safe(user.getPreferredSign()));
        return res;
    }

    private String safe(String v) {
        return v != null ? v : "";
    }
}