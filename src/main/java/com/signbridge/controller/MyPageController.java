package com.signbridge.controller;

import com.signbridge.dto.MyPageDto;
import com.signbridge.entity.User;
import com.signbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MyPageController {

    private final UserRepository userRepository;

    // 사용자 프로필 정보 조회
    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getUserProfile(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(MyPageDto.UserProfile.builder()
                        .email(user.getEmail())
                        .name(user.getName())
                        .orgType(user.getOrgType())
                        .officeName(user.getOfficeName())
                        .preferredSign(user.getPreferredSign())
                        .disabilityGrade(user.getDisabilityGrade())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 기관별 맞춤 데이터 조회 (출입국, 경찰 등)
    @GetMapping("/cases/{email}")
    public ResponseEntity<List<MyPageDto.CaseItem>> getCases(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        List<MyPageDto.CaseItem> cases = new ArrayList<>();

        if (user == null)
            return ResponseEntity.ok(cases);

        // 실제로는 별도의 CaseRepository에서 조회해야 하지만,
        // 테스트를 위해 샘플 데이터를 반환합니다.
        if ("immigration".equals(user.getOrgType())) {
            cases.add(MyPageDto.CaseItem.builder()
                    .id(1L).title("비자 체류자격 변경").applicant("홍길동").status("검토중").build());
        } else if ("police".equals(user.getOrgType())) {
            cases.add(MyPageDto.CaseItem.builder()
                    .id(2L).title("교통사고 진술조서").applicant("김철수").status("접수완료").build());
        }

        return ResponseEntity.ok(cases);
    }
}