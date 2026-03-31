package com.signbridge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.SignResultDto;

@RestController
public class ApiController {

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "running");
        response.put("project", "SignBridge");
        return ResponseEntity.ok(response); // ← ResponseEntity.ok()로 감싸기
    }

    @PostMapping("/api/save-record")
    public ResponseEntity<?> saveSignRecord(@RequestBody SignResultDto record) {
        System.out.println("수어 기록 수신: " + record.getGesture());
        return ResponseEntity.ok("기록 저장 완료");
    }
}