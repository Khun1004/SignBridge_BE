package com.signbridge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    // 서버 상태 확인 — api.jsx의 commonApi.getStatus()에서 호출
    @GetMapping("/api/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "running");
        response.put("project", "SignBridge");
        return ResponseEntity.ok(response);
    }
}