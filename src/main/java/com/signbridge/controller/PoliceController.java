package com.signbridge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/police")
@RequiredArgsConstructor
public class PoliceController {

    @GetMapping("/cases")
    public ResponseEntity<?> getCases(@RequestParam(required = false) String email) {

        List<Map<String, Object>> mockCases = new ArrayList<>();

        // ── Case 1 ──
        Map<String, Object> subject1 = new HashMap<>();
        subject1.put("name", "이호민");
        subject1.put("birth", "1990.04.12");
        subject1.put("disability", "청각장애 1급");
        subject1.put("nationality", "대한민국");
        subject1.put("phone", "010-1234-5678");
        subject1.put("address", "서울시 강남구 테헤란로 123");
        subject1.put("role", "피해자");
        subject1.put("avatar", "🧏");

        Map<String, Object> officer1 = new HashMap<>();
        officer1.put("name", "박민수");
        officer1.put("badge", "12-4892");
        officer1.put("rank", "경장");
        officer1.put("department", "형사과 1팀");
        officer1.put("station", "서울 강남경찰서");
        officer1.put("avatar", "👮");

        Map<String, Object> case1 = new HashMap<>();
        case1.put("id", "POL-2025-001");
        case1.put("subject", subject1);
        case1.put("officer", officer1);
        case1.put("caseType", "피해신고");
        case1.put("caseNum", "2025-강남-4421");
        case1.put("date", "2025.05.12");
        case1.put("time", "16:10");
        case1.put("location", "서울 강남경찰서 조사실 2호");
        case1.put("duration", "15분 22초");
        case1.put("status", "조사 완료");
        case1.put("statusType", "ok");
        case1.put("flagged", false);
        case1.put("signs", List.of("안녕하세요 👋", "도움이 필요해요 🤲", "잠깐만요 ✋"));
        case1.put("voice", List.of("사건 경위를 말씀해주세요", "언제 발생한 일인가요?", "진술서에 서명해 주세요"));
        mockCases.add(case1);

        // ── Case 2 ──
        Map<String, Object> subject2 = new HashMap<>();
        subject2.put("name", "장민호");
        subject2.put("birth", "1988.07.05");
        subject2.put("disability", "청각장애 2급");
        subject2.put("nationality", "대한민국");
        subject2.put("phone", "010-8765-4321");
        subject2.put("address", "경기도 성남시 분당구 정자동 456");
        subject2.put("role", "참고인");
        subject2.put("avatar", "🧏");

        Map<String, Object> officer2 = new HashMap<>();
        officer2.put("name", "최지훈");
        officer2.put("badge", "08-2211");
        officer2.put("rank", "경위");
        officer2.put("department", "형사과 2팀");
        officer2.put("station", "서울 강남경찰서");
        officer2.put("avatar", "👮");

        Map<String, Object> case2 = new HashMap<>();
        case2.put("id", "POL-2025-002");
        case2.put("subject", subject2);
        case2.put("officer", officer2);
        case2.put("caseType", "참고인 조사");
        case2.put("caseNum", "2025-강남-3901");
        case2.put("date", "2025.05.10");
        case2.put("time", "11:30");
        case2.put("location", "서울 강남경찰서 조사실 3호");
        case2.put("duration", "22분 05초");
        case2.put("status", "검토 중");
        case2.put("statusType", "warn");
        case2.put("flagged", true);
        case2.put("signs", List.of("안녕하세요 👋", "괜찮아요 😊", "감사합니다 🙏"));
        case2.put("voice", List.of("목격한 내용을 말씀해주세요", "정확한 시간을 기억하시나요?"));
        mockCases.add(case2);

        // ── Case 3 ──
        Map<String, Object> subject3 = new HashMap<>();
        subject3.put("name", "윤서연");
        subject3.put("birth", "1993.11.30");
        subject3.put("disability", "청각장애 1급");
        subject3.put("nationality", "대한민국");
        subject3.put("phone", "010-3333-6666");
        subject3.put("address", "서울시 서초구 반포대로 789");
        subject3.put("role", "피해자");
        subject3.put("avatar", "🧏");

        Map<String, Object> officer3 = new HashMap<>();
        officer3.put("name", "김현우");
        officer3.put("badge", "15-7731");
        officer3.put("rank", "순경");
        officer3.put("department", "생활안전과");
        officer3.put("station", "서울 서초경찰서");
        officer3.put("avatar", "👮");

        Map<String, Object> case3 = new HashMap<>();
        case3.put("id", "POL-2025-003");
        case3.put("subject", subject3);
        case3.put("officer", officer3);
        case3.put("caseType", "분실물 신고");
        case3.put("caseNum", "2025-서초-1102");
        case3.put("date", "2025.05.09");
        case3.put("time", "13:45");
        case3.put("location", "서울 서초경찰서 민원실");
        case3.put("duration", "8분 10초");
        case3.put("status", "접수 완료");
        case3.put("statusType", "ok");
        case3.put("flagged", false);
        case3.put("signs", List.of("도움이 필요해요 🤲", "감사합니다 🙏"));
        case3.put("voice", List.of("분실 신고를 도와드리겠습니다", "분실한 물건의 특징을 말씀해주세요"));
        mockCases.add(case3);

        return ResponseEntity.ok(mockCases);
    }
}