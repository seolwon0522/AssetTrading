package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long user_idx; // 사용자 인덱스

    @Column(name = "user_id")
    private String userId; // 사용자 ID

    @Column(name = "user_pw")
    private String userPw; // 사용자 비밀번호

    @Column(name = "user_name")
    private String userName; // 사용자 이름

    @Column(name = "business_num")
    private String businessNum; // 사업자 등록번호

    @Column(name = "company_name")
    private String companyName; // 회사명

    @Column(name = "company_address")
    private String companyAddress; // 회사 주소

    @Column(name = "company_industry")
    private String companyIndustry; // 회사 업종

    @Column(name = "company_tel")
    private String companyTel; // 회사 전화번호

    @Transient
    private String description; // 설명 (데이터베이스에 저장되지 않음)

    @Column(name = "join_approved")
    private boolean joinApproved; // 가입 승인 여부

    @Column(name = "registered_at", updatable = false, insertable = false)
    private LocalDateTime registeredAt; // 등록 시간

    @Column(name = "start_date")
    private String startDate; // 개업일자(YYYYMMDD)
}
