package com.example.AssetTrading.Service;

import com.example.AssetTrading.Entity.SellProduct;
import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Repository.ReviewRepository;
import com.example.AssetTrading.Repository.SellProductRepository;
import com.example.AssetTrading.Repository.TransactionRepository;
import com.example.AssetTrading.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 통계는 대부분 읽기 작업만 수행하므로 readOnly 설정
public class StatisticsService {

    private final TransactionRepository transactionRepository;
    private final SellProductRepository sellProductRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 사용자별 월간 거래 통계 
     * 
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월
     * @return 통계 정보
     */
    @Cacheable(value = "userMonthlyStats", key = "#userId + '-' + #year + '-' + #month")
    public Map<String, Object> getUserMonthlyStats(Long userId, int year, int month) {
        log.debug("Fetching monthly stats for user {}, year {}, month {}", userId, year, month);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // 데이터베이스 쿼리를 최소화하기 위해 필요한 데이터를 한 번에 가져옴
        List<Transaction> buyTransactions = transactionRepository.findByBuyerAndCreatedTimeBetween(user, startDate, endDate);
        List<Transaction> sellTransactions = transactionRepository.findBySellerAndCreatedTimeBetween(user, startDate, endDate);
        
        // 통계 계산 (메모리에서 처리)
        int totalBuyCount = buyTransactions.size();
        int totalSellCount = sellTransactions.size();
        
        // COMPLETED 상태의 거래만 필터링
        List<Transaction> completedBuyTransactions = buyTransactions.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
            .collect(Collectors.toList());
            
        List<Transaction> completedSellTransactions = sellTransactions.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
            .collect(Collectors.toList());
            
        int completedBuyCount = completedBuyTransactions.size();
        int completedSellCount = completedSellTransactions.size();
        
        // 총 구매/판매 금액 계산
        Integer totalBuyAmount = transactionRepository.calculateTotalBuyAmount(user, TransactionStatus.COMPLETED);
        Integer totalSellAmount = transactionRepository.calculateTotalSellAmount(user, TransactionStatus.COMPLETED);
        
        // 결과 맵 구성
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("userName", user.getUserName());
        stats.put("companyName", user.getCompanyName());
        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalBuyCount", totalBuyCount);
        stats.put("totalSellCount", totalSellCount);
        stats.put("completedBuyCount", completedBuyCount);
        stats.put("completedSellCount", completedSellCount);
        stats.put("totalBuyAmount", totalBuyAmount != null ? totalBuyAmount : 0);
        stats.put("totalSellAmount", totalSellAmount != null ? totalSellAmount : 0);
        
        // 평점 정보 추가
        double averageRating = reviewRepository.calculateAverageRatingByReviewed(user);
        stats.put("averageRating", Math.round(averageRating * 10) / 10.0); // 소수점 첫째자리까지 반올림
        
        return stats;
    }
    
    /**
     * 사용자별 연간 거래 통계
     * 
     * @param userId 사용자 ID
     * @param year 년도
     * @return 월별 통계 정보
     */
    @Cacheable(value = "userYearlyStats", key = "#userId + '-' + #year")
    public List<Map<String, Object>> getUserYearlyStats(Long userId, int year) {
        log.debug("Fetching yearly stats for user {}, year {}", userId, year);
        
        List<Map<String, Object>> yearlyStats = new ArrayList<>();
        
        // 병렬 처리를 통한 성능 향상 (각 월별 통계는 독립적으로 계산 가능)
        yearlyStats = IntStream.rangeClosed(1, 12)
            .parallel()
            .mapToObj(month -> getUserMonthlyStats(userId, year, month))
            .collect(Collectors.toList());
            
        return yearlyStats;
    }
    
    /**
     * 전체 시스템 통계 (핵심 지표)
     * 
     * @return 시스템 통계 정보
     */
    @Cacheable(value = "systemStats", key = "'system'")
    public Map<String, Object> getSystemStats() {
        log.debug("Calculating system statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 기본 통계 정보
        long totalUsers = userRepository.count();
        long totalProducts = sellProductRepository.count();
        long totalTransactions = transactionRepository.count();
        
        // 거래 상태별 통계
        long completedTransactions = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long processingTransactions = transactionRepository.countByStatus(TransactionStatus.PROCESSING);
        long canceledTransactions = transactionRepository.countByStatus(TransactionStatus.CANCELED);
        long requestedTransactions = transactionRepository.countByStatus(TransactionStatus.REQUESTED);
        
        // 최근 30일 통계
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsers = userRepository.countByRegisteredAtAfter(thirtyDaysAgo);
        long recentTransactions = transactionRepository.countByCreatedTimeAfter(thirtyDaysAgo);
        
        // 거래 완료율과 취소율 계산
        double completionRate = calculatePercentage(completedTransactions, totalTransactions);
        double cancellationRate = calculatePercentage(canceledTransactions, totalTransactions);
        
        // 통계 데이터 취합
        stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        stats.put("totalUsers", totalUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalTransactions", totalTransactions);
        stats.put("completedTransactions", completedTransactions);
        stats.put("processingTransactions", processingTransactions);
        stats.put("canceledTransactions", canceledTransactions);
        stats.put("requestedTransactions", requestedTransactions);
        stats.put("newUsers", newUsers);
        stats.put("recentTransactions", recentTransactions);
        stats.put("completionRate", completionRate);
        stats.put("cancellationRate", cancellationRate);
        
        return stats;
    }
    
    /**
     * 인기 카테고리 통계
     * 
     * @param limit 조회할 카테고리 수
     * @return 카테고리별 상품 수
     */
    @Cacheable(value = "popularCategories", key = "#limit")
    public List<Map<String, Object>> getPopularCategories(int limit) {
        log.debug("Fetching popular categories, limit: {}", limit);
        
        // 상품 카테고리별 집계 (Stream API 활용)
        List<SellProduct> products = sellProductRepository.findAll();
        
        return products.stream()
            .filter(p -> p.getProductCategory() != null && !p.getProductCategory().isEmpty())
            .collect(Collectors.groupingBy(
                SellProduct::getProductCategory,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", entry.getKey());
                categoryData.put("count", entry.getValue());
                
                // 카테고리별 평균 가격 계산
                Double avgPrice = products.stream()
                    .filter(p -> entry.getKey().equals(p.getProductCategory()))
                    .mapToDouble(SellProduct::getProductPrice)
                    .average()
                    .orElse(0.0);
                    
                categoryData.put("averagePrice", Math.round(avgPrice));
                return categoryData;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 일별 거래량 추이
     * 
     * @param days 조회할 일수
     * @return 일별 거래 건수
     */
    @Cacheable(value = "dailyTransactionTrend", key = "#days")
    public List<Map<String, Object>> getDailyTransactionTrend(int days) {
        log.debug("Fetching daily transaction trend for {} days", days);
        
        LocalDate today = LocalDate.now();
        LocalDateTime endDate = today.atTime(23, 59, 59);
        LocalDateTime startDate = today.minusDays(days - 1).atStartOfDay();
        
        // 효율적인 쿼리를 통해 한 번에 날짜별 거래량 조회
        List<Object[]> dailyCounts = transactionRepository.getTransactionCountsByDateRange(startDate, endDate);
        Map<String, Long> countsMap = new HashMap<>();
        
        // 결과를 Map으로 변환 (날짜 -> 거래수)
        for (Object[] row : dailyCounts) {
            String dateStr = ((LocalDate)row[0]).format(DATE_FORMATTER);
            Long count = ((Number)row[1]).longValue();
            countsMap.put(dateStr, count);
        }
        
        // 모든 날짜에 대해 데이터 생성 (거래가 없는 날도 0으로 표시)
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("count", countsMap.getOrDefault(dateStr, 0L));
            
            result.add(dayData);
        }
        
        return result;
    }
    
    /**
     * 거래 완료율 계산
     * 
     * @return 거래 완료율 정보
     */
    @Cacheable(value = "transactionCompletionRate")
    public Map<String, Object> getTransactionCompletionRate() {
        log.debug("Calculating transaction completion rate");
        
        long totalTransactions = transactionRepository.count();
        long completedTransactions = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long processingTransactions = transactionRepository.countByStatus(TransactionStatus.PROCESSING);
        long canceledTransactions = transactionRepository.countByStatus(TransactionStatus.CANCELED);
        long requestedTransactions = transactionRepository.countByStatus(TransactionStatus.REQUESTED);
        
        // 비율 계산
        double completionRate = calculatePercentage(completedTransactions, totalTransactions);
        double cancellationRate = calculatePercentage(canceledTransactions, totalTransactions);
        double processingRate = calculatePercentage(processingTransactions, totalTransactions);
        double requestedRate = calculatePercentage(requestedTransactions, totalTransactions);
        
        // 결과 맵 구성
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("completedTransactions", completedTransactions);
        stats.put("processingTransactions", processingTransactions);
        stats.put("canceledTransactions", canceledTransactions);
        stats.put("requestedTransactions", requestedTransactions);
        stats.put("completionRate", completionRate);
        stats.put("cancellationRate", cancellationRate);
        stats.put("processingRate", processingRate);
        stats.put("requestedRate", requestedRate);
        
        return stats;
    }
    
    /**
     * 백분율 계산 helper 메서드 (소수점 한 자리까지 반올림)
     */
    private double calculatePercentage(long part, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) part / total * 1000) / 10.0;
    }
} 