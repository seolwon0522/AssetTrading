package com.example.AssetTrading.Service;

import com.example.AssetTrading.Entity.Review;
import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.TransactionStatus;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Exception.ResourceNotFoundException;
import com.example.AssetTrading.Repository.ReviewRepository;
import com.example.AssetTrading.Repository.TransactionRepository;
import com.example.AssetTrading.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    /**
     * 리뷰 생성
     * 
     * @param transactionId 거래 ID
     * @param reviewerId 리뷰 작성자 ID
     * @param rating 평점 (1-5)
     * @param content 리뷰 내용
     * @return 생성된 리뷰
     * @throws ResourceNotFoundException 거래나 사용자가 존재하지 않는 경우
     * @throws IllegalStateException 거래가 완료되지 않았거나 이미 리뷰를 작성한 경우
     * @throws IllegalArgumentException 유효하지 않은 입력값인 경우
     */
    public Review createReview(Long transactionId, Long reviewerId, Integer rating, String content) {
        log.debug("리뷰 생성 요청: transactionId={}, reviewerId={}, rating={}", transactionId, reviewerId, rating);
        
        // 입력값 유효성 검사
        validateRating(rating);
        validateContent(content);
        
        // 거래 정보 확인
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 거래가 존재하지 않습니다. transaction_idx: " + transactionId));
        
        // 거래 완료 상태 확인
        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            log.warn("미완료 거래에 대한 리뷰 작성 시도: transactionId={}, status={}", 
                    transactionId, transaction.getStatus());
            throw new IllegalStateException("완료된 거래에 대해서만 리뷰를 작성할 수 있습니다. 현재 상태: " + transaction.getStatus());
        }
        
        // 리뷰 작성자 확인
        User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + reviewerId));
        
        // 리뷰 작성자가 해당 거래의 구매자 또는 판매자인지 확인
        boolean isBuyer = reviewer.getUser_idx().equals(transaction.getBuyer().getUser_idx());
        boolean isSeller = reviewer.getUser_idx().equals(transaction.getSeller().getUser_idx());
        
        if (!isBuyer && !isSeller) {
            log.warn("거래 참여자가 아닌 사용자의 리뷰 작성 시도: reviewerId={}, transactionId={}", 
                    reviewerId, transactionId);
            throw new IllegalArgumentException("해당 거래의 참여자만 리뷰를 작성할 수 있습니다.");
        }
        
        // 리뷰 대상자 결정 (작성자가 구매자면 판매자가 대상, 작성자가 판매자면 구매자가 대상)
        User reviewed = isBuyer ? transaction.getSeller() : transaction.getBuyer();
        
        // 이미 작성한 리뷰가 있는지 확인
        Optional<Review> existingReview = reviewRepository.findByTransactionAndReviewer(transaction, reviewer);
        if (existingReview.isPresent()) {
            log.warn("이미 작성된 리뷰가 있음: reviewerId={}, transactionId={}", reviewerId, transactionId);
            throw new IllegalStateException("이미 이 거래에 대한 리뷰를 작성했습니다.");
        }
        
        // 리뷰 생성
        Review review = Review.builder()
            .transaction(transaction)
            .reviewer(reviewer)
            .reviewed(reviewed)
            .rating(rating)
            .content(content)
            .createdAt(LocalDateTime.now())
            .isDeleted(false)
            .build();
        
        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 생성 완료: reviewId={}, transactionId={}, rating={}", 
                savedReview.getId(), transactionId, rating);
        
        // 알림 전송
        notificationService.createTransactionNotification(
            reviewed.getUser_idx(),
            transaction.getTransactionIdx(),
            reviewer.getUserName() + "님이 거래에 대한 리뷰를 작성했습니다."
        );
        
        return savedReview;
    }
    
    /**
     * 리뷰 수정
     * 
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID (권한 확인용)
     * @param rating 수정할 평점
     * @param content 수정할 내용
     * @return 수정된 리뷰
     * @throws ResourceNotFoundException 리뷰가 존재하지 않는 경우
     * @throws IllegalArgumentException 권한이 없는 경우
     */
    @CacheEvict(value = {"userReviews", "transactionReviews", "userRatings"}, 
                key = "#userId", allEntries = true)
    public Review updateReview(Long reviewId, Long userId, Integer rating, String content) {
        log.debug("리뷰 수정 요청: reviewId={}, userId={}, rating={}", reviewId, userId, rating);
        
        // 입력값 유효성 검사
        validateRating(rating);
        validateContent(content);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 리뷰가 존재하지 않습니다. review_idx: " + reviewId));
        
        // 리뷰 작성자 확인
        if (!review.getReviewer().getUser_idx().equals(userId)) {
            log.warn("리뷰 수정 권한 없음: reviewId={}, userId={}, reviewerId={}", 
                    reviewId, userId, review.getReviewer().getUser_idx());
            throw new IllegalArgumentException("리뷰 작성자만 수정할 수 있습니다.");
        }
        
        // 삭제된 리뷰인지 확인
        if (review.getIsDeleted()) {
            log.warn("삭제된 리뷰 수정 시도: reviewId={}", reviewId);
            throw new IllegalStateException("삭제된 리뷰는 수정할 수 없습니다.");
        }
        
        // 리뷰 정보 업데이트
        review.setRating(rating);
        review.setContent(content);
        review.setUpdatedAt(LocalDateTime.now());
        
        Review updatedReview = reviewRepository.save(review);
        log.info("리뷰 수정 완료: reviewId={}, rating={}", reviewId, rating);
        
        // 알림 전송
        notificationService.createTransactionNotification(
            review.getReviewed().getUser_idx(),
            review.getTransaction().getTransactionIdx(),
            review.getReviewer().getUserName() + "님이 리뷰를 수정했습니다."
        );
        
        return updatedReview;
    }
    
    /**
     * 리뷰 삭제 (소프트 딜리트)
     * 
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID (권한 확인용)
     * @throws ResourceNotFoundException 리뷰가 존재하지 않는 경우
     * @throws IllegalArgumentException 권한이 없는 경우
     */
    @CacheEvict(value = {"userReviews", "transactionReviews", "userRatings"}, 
                key = "#userId", allEntries = true)
    public void deleteReview(Long reviewId, Long userId) {
        log.debug("리뷰 삭제 요청: reviewId={}, userId={}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 리뷰가 존재하지 않습니다. review_idx: " + reviewId));
        
        // 리뷰 작성자 확인
        if (!review.getReviewer().getUser_idx().equals(userId)) {
            log.warn("리뷰 삭제 권한 없음: reviewId={}, userId={}, reviewerId={}", 
                    reviewId, userId, review.getReviewer().getUser_idx());
            throw new IllegalArgumentException("리뷰 작성자만 삭제할 수 있습니다.");
        }
        
        // 이미 삭제된 리뷰인지 확인
        if (review.getIsDeleted()) {
            log.warn("이미 삭제된 리뷰: reviewId={}", reviewId);
            return;
        }
        
        // 소프트 딜리트 처리
        review.setIsDeleted(true);
        review.setUpdatedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        log.info("리뷰 삭제 완료: reviewId={}", reviewId);
        
        // 알림 전송 (선택적)
        notificationService.createTransactionNotification(
            review.getReviewed().getUser_idx(),
            review.getTransaction().getTransactionIdx(),
            review.getReviewer().getUserName() + "님의 리뷰가 삭제되었습니다."
        );
    }
    
    /**
     * 특정 사용자가 받은 리뷰 조회
     * 
     * @param userId 사용자 ID
     * @return 리뷰 목록
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userReviews", key = "'received-' + #userId")
    public List<Review> getReviewsByReviewedUser(Long userId) {
        log.debug("사용자 수신 리뷰 조회: userId={}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        List<Review> reviews = reviewRepository.findByReviewedAndIsDeletedFalseOrderByCreatedAtDesc(user);
        log.debug("리뷰 조회 결과: userId={}, count={}", userId, reviews.size());
        
        return reviews;
    }
    
    /**
     * 특정 사용자가 작성한 리뷰 조회
     * 
     * @param userId 사용자 ID
     * @return 리뷰 목록
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userReviews", key = "'written-' + #userId")
    public List<Review> getReviewsByReviewerUser(Long userId) {
        log.debug("사용자 작성 리뷰 조회: userId={}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        List<Review> reviews = reviewRepository.findByReviewerAndIsDeletedFalseOrderByCreatedAtDesc(user);
        log.debug("리뷰 조회 결과: userId={}, count={}", userId, reviews.size());
        
        return reviews;
    }
    
    /**
     * 특정 거래의 리뷰 조회
     * 
     * @param transactionId 거래 ID
     * @return 리뷰 목록
     * @throws ResourceNotFoundException 거래가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "transactionReviews", key = "#transactionId")
    public List<Review> getReviewsByTransaction(Long transactionId) {
        log.debug("거래 리뷰 조회: transactionId={}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 거래가 존재하지 않습니다. transaction_idx: " + transactionId));
        
        List<Review> reviews = reviewRepository.findByTransactionAndIsDeletedFalseOrderByCreatedAtDesc(transaction);
        log.debug("리뷰 조회 결과: transactionId={}, count={}", transactionId, reviews.size());
        
        return reviews;
    }
    
    /**
     * 사용자 평균 평점 계산
     * 
     * @param userId 사용자 ID
     * @return 평균 평점 (소수점 1자리까지)
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userRatings", key = "#userId")
    public double calculateAverageRating(Long userId) {
        log.debug("사용자 평균 평점 계산: userId={}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        double avgRating = reviewRepository.calculateAverageRatingByReviewed(user);
        // 소수점 첫째자리까지 반올림
        double roundedRating = Math.round(avgRating * 10) / 10.0;
        
        log.debug("평균 평점 계산 결과: userId={}, avgRating={}", userId, roundedRating);
        return roundedRating;
    }
    
    /**
     * 사용자 평점 분포 조회
     * 
     * @param userId 사용자 ID
     * @return 평점별 리뷰 수
     * @throws ResourceNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userRatings", key = "'distribution-' + #userId")
    public Map<Integer, Long> getRatingDistribution(Long userId) {
        log.debug("사용자 평점 분포 조회: userId={}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다. user_idx: " + userId));
        
        List<Object[]> ratingCounts = reviewRepository.countReviewsByRating(user);
        
        // 1-5점까지 모든 평점에 대해 결과 매핑
        Map<Integer, Long> distribution = ratingCounts.stream()
            .collect(Collectors.toMap(
                data -> ((Number) data[0]).intValue(),  // 평점
                data -> ((Number) data[1]).longValue()) // 개수
            );
        
        // 결과가 없는 평점은 0으로 초기화
        for (int i = MIN_RATING; i <= MAX_RATING; i++) {
            distribution.putIfAbsent(i, 0L);
        }
        
        log.debug("평점 분포 조회 결과: userId={}, distribution={}", userId, distribution);
        return distribution;
    }
    
    /**
     * 평점 유효성 검사
     *
     * @param rating 평점
     * @throws IllegalArgumentException 유효하지 않은 평점인 경우
     */
    private void validateRating(Integer rating) {
        if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("평점은 " + MIN_RATING + "점에서 " + MAX_RATING + "점 사이여야 합니다.");
        }
    }
    
    /**
     * 리뷰 내용 유효성 검사
     *
     * @param content 리뷰 내용
     * @throws IllegalArgumentException 유효하지 않은 내용인 경우
     */
    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력하세요.");
        }
        
        if (content.length() > 1000) {
            throw new IllegalArgumentException("리뷰 내용은 1000자를 초과할 수 없습니다.");
        }
    }
} 