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
import java.util.EnumMap;
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
     */
    @Cacheable(value = "userMonthlyStats", key = "#userId + '-' + #year + '-' + #month")
    public Map<String, Object> getUserMonthlyStats(Long userId, int year, int month) {
        log.debug("사용자 {} 의 {}년 {}월 월간 통계 조회", userId, year, month);
        
        // 사용자와 날짜 범위 설정
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // 구매/판매 거래 통계
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("userName", user.getUserName());
        stats.put("companyName", user.getCompanyName());
        stats.put("year", year);
        stats.put("month", month);
        
        // 거래 건수 통계
        addTransactionCounts(stats, user, startDate, endDate);
        
        // 거래 금액 통계
        addTransactionAmounts(stats, user);
        
        // 평점 정보 추가
        double averageRating = reviewRepository.calculateAverageRatingByReviewed(user);
        stats.put("averageRating", Math.round(averageRating * 10) / 10.0); // 소수점 첫째자리까지 반올림
        
        return stats;
    }
    
    /**
     * 거래 건수 통계 정보 추가
     */
    private void addTransactionCounts(Map<String, Object> stats, User user, LocalDateTime startDate, LocalDateTime endDate) {
        // 거래 데이터 조회
        List<Transaction> buyTransactions = transactionRepository.findByBuyerAndCreatedTimeBetween(user, startDate, endDate);
        List<Transaction> sellTransactions = transactionRepository.findBySellerAndCreatedTimeBetween(user, startDate, endDate);
        
        // 전체 거래 건수
        stats.put("totalBuyCount", buyTransactions.size());
        stats.put("totalSellCount", sellTransactions.size());
        
        // 완료된 거래 건수
        long completedBuyCount = countByStatus(buyTransactions, TransactionStatus.COMPLETED);
        long completedSellCount = countByStatus(sellTransactions, TransactionStatus.COMPLETED);
        
        stats.put("completedBuyCount", completedBuyCount);
        stats.put("completedSellCount", completedSellCount);
    }
    
    /**
     * 거래 금액 통계 정보 추가
     */
    private void addTransactionAmounts(Map<String, Object> stats, User user) {
        Integer totalBuyAmount = transactionRepository.calculateTotalBuyAmount(user, TransactionStatus.COMPLETED);
        Integer totalSellAmount = transactionRepository.calculateTotalSellAmount(user, TransactionStatus.COMPLETED);
        
        stats.put("totalBuyAmount", totalBuyAmount != null ? totalBuyAmount : 0);
        stats.put("totalSellAmount", totalSellAmount != null ? totalSellAmount : 0);
    }
    
    /**
     * 특정 상태의 거래 수 계산
     */
    private long countByStatus(List<Transaction> transactions, TransactionStatus status) {
        return transactions.stream()
            .filter(t -> t.getStatus() == status)
            .count();
    }
    
    /**
     * 사용자별 연간 거래 통계
     */
    @Cacheable(value = "userYearlyStats", key = "#userId + '-' + #year")
    public List<Map<String, Object>> getUserYearlyStats(Long userId, int year) {
        log.debug("사용자 {} 의 {}년 연간 통계 조회", userId, year);
        
        // 병렬 처리를 통한 성능 향상 (각 월별 통계는 독립적으로 계산 가능)
        return IntStream.rangeClosed(1, 12)
            .parallel()
            .mapToObj(month -> getUserMonthlyStats(userId, year, month))
            .collect(Collectors.toList());
    }
    
    /**
     * 전체 시스템 통계 (핵심 지표)
     */
    @Cacheable(value = "systemStats", key = "'system'")
    public Map<String, Object> getSystemStats() {
        log.debug("시스템 전체 통계 계산 중");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 기본 통계 정보
        stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProducts", sellProductRepository.count());
        stats.put("totalTransactions", transactionRepository.count());
        
        // 거래 상태별 통계
        Map<TransactionStatus, Long> statusCounts = getTransactionStatusCounts();
        statusCounts.forEach((status, count) -> 
            stats.put(status.name().toLowerCase() + "Transactions", count));
        
        // 최근 30일 통계
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("newUsers", userRepository.countByRegisteredAtAfter(thirtyDaysAgo));
        stats.put("recentTransactions", transactionRepository.countByCreatedTimeAfter(thirtyDaysAgo));
        
        // 거래 완료율과 취소율 계산
        long totalTx = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        addCompletionRates(stats, statusCounts, totalTx);
        
        return stats;
    }
    
    /**
     * 거래 상태별 건수 조회
     */
    private Map<TransactionStatus, Long> getTransactionStatusCounts() {
        Map<TransactionStatus, Long> counts = new EnumMap<>(TransactionStatus.class);
        for (TransactionStatus status : TransactionStatus.values()) {
            counts.put(status, transactionRepository.countByStatus(status));
        }
        return counts;
    }
    
    /**
     * 거래 완료율 정보 추가
     */
    private void addCompletionRates(Map<String, Object> stats, Map<TransactionStatus, Long> statusCounts, long total) {
        stats.put("completionRate", calculatePercentage(statusCounts.getOrDefault(TransactionStatus.COMPLETED, 0L), total));
        stats.put("cancellationRate", calculatePercentage(statusCounts.getOrDefault(TransactionStatus.CANCELED, 0L), total));
        stats.put("processingRate", calculatePercentage(statusCounts.getOrDefault(TransactionStatus.PROCESSING, 0L), total));
        stats.put("requestedRate", calculatePercentage(statusCounts.getOrDefault(TransactionStatus.REQUESTED, 0L), total));
    }
    
    /**
     * 인기 카테고리 통계
     */
    @Cacheable(value = "popularCategories", key = "#limit")
    public List<Map<String, Object>> getPopularCategories(int limit) {
        log.debug("인기 카테고리 조회, 제한 수: {}", limit);
        
        // 카테고리별 상품 수 및 평균 가격 계산
        Map<String, List<SellProduct>> productsByCategory = sellProductRepository.findAll().stream()
            .filter(p -> p.getProductCategory() != null && !p.getProductCategory().isEmpty())
            .collect(Collectors.groupingBy(SellProduct::getProductCategory));
        
        return productsByCategory.entrySet().stream()
            .map(entry -> {
                Map<String, Object> data = new HashMap<>();
                data.put("category", entry.getKey());
                data.put("count", entry.getValue().size());
                
                // 카테고리별 평균 가격 계산
                double avgPrice = entry.getValue().stream()
                    .mapToDouble(SellProduct::getProductPrice)
                    .average()
                    .orElse(0.0);
                    
                data.put("averagePrice", Math.round(avgPrice));
                return data;
            })
            .sorted((a, b) -> Long.compare((Long)b.get("count"), (Long)a.get("count")))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * 일별 거래량 추이
     */
    @Cacheable(value = "dailyTransactionTrend", key = "#days")
    public List<Map<String, Object>> getDailyTransactionTrend(int days) {
        log.debug("{}일간의 일별 거래량 추이 조회", days);
        
        LocalDate today = LocalDate.now();
        LocalDateTime endDate = today.atTime(23, 59, 59);
        LocalDateTime startDate = today.minusDays(days - 1).atStartOfDay();
        
        // 날짜별 거래량 조회 및 Map으로 변환
        Map<String, Long> countsMap = getDateTransactionCountsMap(startDate, endDate);
        
        // 결과 데이터 생성 (날짜순으로)
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
     * 날짜별 거래량 조회 및 Map 변환
     */
    private Map<String, Long> getDateTransactionCountsMap(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> dailyCounts = transactionRepository.getTransactionCountsByDateRange(startDate, endDate);
        Map<String, Long> countsMap = new HashMap<>();
        
        for (Object[] row : dailyCounts) {
            String dateStr = ((LocalDate)row[0]).format(DATE_FORMATTER);
            Long count = ((Number)row[1]).longValue();
            countsMap.put(dateStr, count);
        }
        
        return countsMap;
    }
    
    /**
     * 거래 완료율 계산
     */
    @Cacheable(value = "transactionCompletionRate")
    public Map<String, Object> getTransactionCompletionRate() {
        log.debug("거래 완료율 계산 중");
        
        // 거래 상태별 건수 조회
        Map<TransactionStatus, Long> statusCounts = getTransactionStatusCounts();
        long totalTransactions = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        
        // 결과 맵 구성
        Map<String, Object> stats = new HashMap<>();
        
        // 건수 정보 추가
        stats.put("totalTransactions", totalTransactions);
        statusCounts.forEach((status, count) -> 
            stats.put(status.name().toLowerCase() + "Transactions", count));
        
        // 비율 정보 추가
        addCompletionRates(stats, statusCounts, totalTransactions);
        
        return stats;
    }
    
    /**
     * 백분율 계산 (소수점 한 자리까지 반올림)
     */
    private double calculatePercentage(long part, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) part / total * 1000) / 10.0;
    }
} 