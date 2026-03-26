package com.signbridge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity; // 추가
// 아래 4줄이 추가되어야 에러가 사라집니다.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // 추가
import org.springframework.web.bind.annotation.RequestBody; // 추가
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.SignResultDto;

@RestController
public class ApiController {

    @GetMapping("/api/status")
    public Map<String, String> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "running");
        response.put("project", "SignBridge");
        return response;
    }

    @PostMapping("/api/save-record")
    public ResponseEntity<?> saveSignRecord(@RequestBody SignResultDto record) {
        // 전송된 데이터 확인용 로그
        System.out.println("수어 기록 수신: " + record.getGesture());
        return ResponseEntity.ok("기록 저장 완료");
    }
}
