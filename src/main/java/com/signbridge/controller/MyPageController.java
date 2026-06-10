package com.signbridge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.signbridge.dto.MyPageDto;
import com.signbridge.entity.User;
import com.signbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final UserRepository userRepository;

    // ── 프로필 조회 ──────────────────────────────────────────
    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getProfile(@PathVariable("email") String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildProfileResponse(userOpt.get()));
    }

    // ── 프로필 수정 — all editable fields ───────────────────
    @PatchMapping("/profile/{email}")
    public ResponseEntity<?> updateProfile(
            @PathVariable("email") String email,
            @RequestBody Map<String, String> req) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        // 이름 — required
        String name = req.get("name");
        if (name != null && !name.isBlank()) user.setName(name.trim());

        // 장애 등급
        if (req.containsKey("disabilityGrade"))
            user.setDisabilityGrade(req.get("disabilityGrade").trim());

        // 주로 사용하는 수어
        if (req.containsKey("preferredSign"))
            user.setPreferredSign(req.get("preferredSign").trim());

        // 주소
        if (req.containsKey("address"))
            user.setAddress(req.get("address").trim());

        // 상세주소
        if (req.containsKey("addressDetail"))
            user.setAddressDetail(req.get("addressDetail").trim());

        // 우편번호
        if (req.containsKey("zonecode"))
            user.setZonecode(req.get("zonecode").trim());

        userRepository.save(user);
        return ResponseEntity.ok(buildProfileResponse(user));
    }

    // ── 기관별 케이스 목록 ───────────────────────────────────
    @GetMapping("/cases/{email}")
    public ResponseEntity<List<MyPageDto.CaseItem>> getCases(@PathVariable("email") String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        List<MyPageDto.CaseItem> cases = new ArrayList<>();
        if (user == null) return ResponseEntity.ok(cases);

        if ("immigration".equals(user.getOrgType())) {
            cases.add(MyPageDto.CaseItem.builder()
                    .id(1L).title("비자 체류자격 변경").applicant("홍길동").status("검토중").build());
        } else if ("police".equals(user.getOrgType())) {
            cases.add(MyPageDto.CaseItem.builder()
                    .id(2L).title("교통사고 진술조서").applicant("김철수").status("접수완료").build());
        }
        return ResponseEntity.ok(cases);
    }

    // ── User → response map ──────────────────────────────────
    private Map<String, String> buildProfileResponse(User user) {
        Map<String, String> res = new HashMap<>();
        res.put("email",           safe(user.getEmail()));
        res.put("name",            safe(user.getName()));
        res.put("orgType",         safe(user.getOrgType()));
        res.put("officeName",      safe(user.getOfficeName()));
        res.put("orgCode",         safe(user.getOrgCode()));
        res.put("address",         safe(user.getAddress()));
        res.put("addressDetail",   safe(user.getAddressDetail()));
        res.put("zonecode",        safe(user.getZonecode()));
        res.put("disabilityGrade", safe(user.getDisabilityGrade()));
        res.put("preferredSign",   safe(user.getPreferredSign()));
        return res;
    }

    private String safe(String v) { return v != null ? v : ""; }
}