package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.Review;
import com.example.AssetTrading.Entity.Transaction;
import com.example.AssetTrading.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 거래와 리뷰 작성자로 리뷰 조회
     */
    Optional<Review> findByTransactionAndReviewer(Transaction transaction, User reviewer);
    
    /**
     * 특정 사용자가 받은 리뷰 목록 조회 (삭제되지 않은 것만)
     */
    List<Review> findByReviewedAndIsDeletedFalseOrderByCreatedAtDesc(User reviewed);
    
    /**
     * 특정 사용자가 작성한 리뷰 목록 조회 (삭제되지 않은 것만)
     */
    List<Review> findByReviewerAndIsDeletedFalseOrderByCreatedAtDesc(User reviewer);
    
    /**
     * 특정 거래의 리뷰 목록 조회 (삭제되지 않은 것만)
     */
    List<Review> findByTransactionAndIsDeletedFalseOrderByCreatedAtDesc(Transaction transaction);
    
    /**
     * 특정 사용자의 평균 평점 계산
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.reviewed = :reviewed AND r.isDeleted = false")
    double calculateAverageRatingByReviewed(User reviewed);
    
    /**
     * 평점별 리뷰 개수 조회
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.reviewed = :reviewed AND r.isDeleted = false GROUP BY r.rating")
    List<Object[]> countReviewsByRating(User reviewed);
} 