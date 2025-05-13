package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.SellProduct;
import com.example.AssetTrading.Entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellProductRepository extends JpaRepository<SellProduct, Long> {
    List<SellProduct> findByProductTitleContaining(String keyword);
    
    Page<SellProduct> findByProductTitleContaining(String keyword, Pageable pageable);
    
    List<SellProduct> findByProductStatus(ProductStatus productStatus);
    
    Page<SellProduct> findByProductStatus(ProductStatus productStatus, Pageable pageable);
    
    List<SellProduct> findByProductCategoryAndProductStatus(String category, ProductStatus status);
    
    Page<SellProduct> findByProductCategoryAndProductStatus(String category, ProductStatus status, Pageable pageable);
    
    List<SellProduct> findBySellerUserIdx(Long sellerUserIdx);
    
    Page<SellProduct> findBySellerUserIdx(Long sellerUserIdx, Pageable pageable);
    
    List<SellProduct> findByFeaturedTrue();
    
    @Query("SELECT p FROM SellProduct p WHERE p.productTags LIKE %:tag%")
    List<SellProduct> findByProductTag(@Param("tag") String tag);
    
    @Modifying
    @Query("UPDATE SellProduct p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);
    
    @Query("SELECT p FROM SellProduct p WHERE p.productPrice BETWEEN :minPrice AND :maxPrice")
    List<SellProduct> findByPriceRange(@Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice);
    
    List<SellProduct> findTop5ByOrderByViewCountDesc();
}
