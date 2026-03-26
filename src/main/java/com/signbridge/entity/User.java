package com.signbridge.entity;

// 반드시 jakarta.persistence 패키지를 사용해야 합니다.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // org.hibernate.annotations.Table 대신 jakarta.persistence.Table 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id // org.springframework.data.annotation.Id에서 jakarta.persistence.Id로 변경
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @Column(nullable = false)
    private String orgType;

    private String officeName;
    private String orgCode;
    private String address;
    private String addressDetail;
    private String zonecode;
    private String disabilityGrade;
    private String preferredSign;
}