package com.signbridge.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.signbridge.dto.UserDto;
import com.signbridge.entity.User;
import com.signbridge.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // React 포트 허용
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // 실제로는 PasswordEncoder 사용 권장
                .name(request.getName())
                .orgType(request.getOrgType())
                .officeName(request.getOfficeName())
                .orgCode(request.getOrgCode())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .zonecode(request.getZonecode())
                .disabilityGrade(request.getDisabilityGrade())
                .preferredSign(request.getPreferredSign())
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto.LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())) {
            User user = userOpt.get();

            // 프론트엔드 App.jsx의 handleLogin(name, type) 형식에 맞춘 응답
            // 기관일 경우 officeName을, 개인일 경우 name을 반환
            String displayName = (user.getOrgType().equals("personal"))
                    ? user.getName()
                    : user.getOfficeName();

            return ResponseEntity.ok(UserDto.LoginResponse.builder()
                    .name(displayName)
                    .orgType(user.getOrgType())
                    .email(user.getEmail())
                    .message("로그인 성공")
                    .build());
        }

        return ResponseEntity.status(401).body("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
