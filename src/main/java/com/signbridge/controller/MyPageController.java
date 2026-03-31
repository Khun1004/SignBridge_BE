package com.signbridge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 사용자 프로필 정보 조회
    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getProfile(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        Map<String, String> response = new HashMap<>();
        response.put("email", user.getEmail() != null ? user.getEmail() : "");
        response.put("name", user.getName() != null ? user.getName() : "");
        response.put("orgType", user.getOrgType() != null ? user.getOrgType() : "");
        response.put("officeName", user.getOfficeName() != null ? user.getOfficeName() : "");
        response.put("orgCode", user.getOrgCode() != null ? user.getOrgCode() : "");
        response.put("address", user.getAddress() != null ? user.getAddress() : "");
        response.put("addressDetail", user.getAddressDetail() != null ? user.getAddressDetail() : "");
        response.put("zonecode", user.getZonecode() != null ? user.getZonecode() : "");
        response.put("disabilityGrade", user.getDisabilityGrade() != null ? user.getDisabilityGrade() : "");
        response.put("preferredSign", user.getPreferredSign() != null ? user.getPreferredSign() : "");

        return ResponseEntity.ok(response);
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