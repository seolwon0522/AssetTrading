package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);
    
    Optional<User> findByUserId(String userId);
    
    // 이메일로 사용자 찾기 (아이디 찾기 기능용)
    Optional<User> findByCompanyEmail(String companyEmail);
    
    // 회사명으로 사용자 검색
    List<User> findByCompanyNameContaining(String companyName);
    
    // 업종으로 사용자 검색
    List<User> findByCompanyIndustryContaining(String industry);
    
    // 최근 가입자 목록 조회
    List<User> findByRegisteredAtAfterOrderByRegisteredAtDesc(LocalDateTime dateTime);
    
    // 가입 승인 대기 사용자 목록 조회
    List<User> findByJoinApprovedFalse();
    
    // 최근 가입자 수 집계
    long countByRegisteredAtAfter(LocalDateTime dateTime);
    
    // 지역별 가입자 통계
    @Query("SELECT u.companyAddress, COUNT(u) FROM User u GROUP BY u.companyAddress")
    List<Object[]> countUsersByAddress();
    
    // 업종별 가입자 통계
    @Query("SELECT u.companyIndustry, COUNT(u) FROM User u GROUP BY u.companyIndustry")
    List<Object[]> countUsersByIndustry();
}