package com.example.AssetTrading.Service;

import com.example.AssetTrading.Dto.SellProductRequestDto;
import com.example.AssetTrading.Dto.SellProductResponseDto;
import com.example.AssetTrading.Dto.UserResponseDto;
import com.example.AssetTrading.Entity.SellProduct;
import com.example.AssetTrading.Entity.User;
import com.example.AssetTrading.Entity.ProductStatus;
import com.example.AssetTrading.Repository.SellProductRepository;
import com.example.AssetTrading.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 판매 상품 관리 서비스
 * 상품 등록, 조회, 수정, 삭제 및 다양한 조건별 상품 검색 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class SellProductService {

    private final SellProductRepository sellProductRepository;
    private final UserRepository userRepository;

    /**
     * 판매 상품 등록
     * 
     * @param dto 상품 정보가 담긴 DTO
     * @return 등록된 상품 정보
     */
    public SellProductResponseDto createSellProduct(SellProductRequestDto dto) {
        SellProduct product = dto.toEntity();
        product.setProductStatus(ProductStatus.AVAILABLE);
        SellProduct saved = sellProductRepository.save(product);
        return SellProductResponseDto.fromEntity(saved);
    }

    /**
     * 세션 정보를 이용한 판매 상품 등록
     * 현재 로그인된 사용자 정보를 세션에서 가져와 판매자로 설정합니다.
     * 
     * @param dto 상품 정보가 담긴 DTO
     * @param session HTTP 세션
     * @return 등록된 상품 정보
     * @throws IllegalStateException 로그인 정보가 없을 경우
     * @throws IllegalArgumentException 사용자를 찾을 수 없을 경우
     */
    public SellProductResponseDto createSellProductWithSession(SellProductRequestDto dto, HttpSession session) {
        Object userObj = session.getAttribute("LOGIN_USER");

        if (!(userObj instanceof UserResponseDto userDto)) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }

        // 실제 User 엔티티를 조회
        User user = userRepository.findByUserId(userDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        dto.setSellerUserIdx(user.getUser_idx());
        return createSellProduct(dto);
    }

    /**
     * 키워드로 모든 판매 상품 검색
     * 
     * @param keyword 검색 키워드
     * @return 검색된 상품 목록
     */
    public List<SellProductResponseDto> getAllSellProducts(String keyword) {
        List<SellProduct> products = sellProductRepository.findByProductTitleContaining(keyword);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 키워드로 판매 상품 페이징 검색
     * 정렬 기능 포함
     * 
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준 필드
     * @param direction 정렬 방향 (ASC, DESC)
     * @return 페이징된 상품 목록
     */
    public Page<SellProductResponseDto> getAllSellProductsPaged(String keyword, int page, int size, String sortBy, String direction) {
        // 정렬 방향 설정 (DESC면 내림차순, 아니면 오름차순)
        Sort sort = direction.equalsIgnoreCase("DESC") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SellProduct> productPage = sellProductRepository.findByProductTitleContaining(keyword, pageable);
        return productPage.map(SellProductResponseDto::fromEntity);
    }

    /**
     * ID로 판매 상품 조회 (조회수 증가 포함)
     * 
     * @param productId 상품 ID
     * @return 상품 정보
     * @throws IllegalArgumentException 상품을 찾을 수 없을 경우
     */
    @Transactional
    public SellProductResponseDto getSellProductById(Long productId) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        // 조회수 증가
        sellProductRepository.incrementViewCount(productId);
        
        return SellProductResponseDto.fromEntity(product);
    }

    /**
     * 판매 상품 삭제
     * 
     * @param productId 삭제할 상품 ID
     */
    public void deleteSellProduct(Long productId) {
        sellProductRepository.deleteById(productId);
    }

    /**
     * 판매 상품 정보 수정
     * 
     * @param productId 수정할 상품 ID
     * @param dto 수정할 상품 정보
     * @return 수정된 상품 정보
     * @throws IllegalArgumentException 상품을 찾을 수 없을 경우
     */
    public SellProductResponseDto updateSellProduct(Long productId, SellProductRequestDto dto) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        // 상품 정보 업데이트
        product.setProductTitle(dto.getProductTitle());
        product.setProductDesc(dto.getProductDesc());
        product.setProductImg(dto.getProductImg());
        product.setProductPrice(dto.getProductPrice());
        product.setProductQuantity(dto.getProductQuantity());
        product.setProductAvailDate(dto.getProductAvailDate());
        product.setProductCategory(dto.getProductCategory());
        product.setProductTags(dto.getProductTags());
        
        // Featured 필드는 null이 아닐 때만 업데이트
        if (dto.getFeatured() != null) {
            product.setFeatured(dto.getFeatured());
        }
        
        SellProduct updated = sellProductRepository.save(product);
        return SellProductResponseDto.fromEntity(updated);
    }

    /**
     * 판매 가능 여부 수정
     * 
     * @param productId 수정할 상품 ID
     * @param isSellingAvailable true면 판매 가능, false면 판매 불가
     * @throws IllegalArgumentException 상품을 찾을 수 없을 경우
     */
    public void updateStatus(Long productId, boolean isSellingAvailable) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setProductStatus(isSellingAvailable ? ProductStatus.AVAILABLE : ProductStatus.UNAVAILABLE);
        sellProductRepository.save(product);
    }
    
    /**
     * 상품 상태 수정
     * 
     * @param productId 수정할 상품 ID
     * @param status 설정할 상태 (AVAILABLE, UNAVAILABLE, RESERVED, SOLD_OUT)
     * @throws IllegalArgumentException 상품을 찾을 수 없거나 유효하지 않은 상태일 경우
     */
    public void updateProductStatus(Long productId, String status) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        try {
            // 문자열 상태를 Enum 타입으로 변환
            ProductStatus newStatus = ProductStatus.valueOf(status.toUpperCase());
            product.setProductStatus(newStatus);
            sellProductRepository.save(product);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상품 상태입니다: " + status);
        }
    }

    /**
     * 판매 가능한 모든 상품 조회
     * 
     * @return 판매 가능한 상품 목록
     */
    public List<SellProductResponseDto> getVisibleProducts() {
        List<SellProduct> products = sellProductRepository.findByProductStatus(ProductStatus.AVAILABLE);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 판매 가능한 상품 페이징 조회
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 판매 가능 상품 목록
     */
    public Page<SellProductResponseDto> getVisibleProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SellProduct> productPage = sellProductRepository.findByProductStatus(ProductStatus.AVAILABLE, pageable);
        return productPage.map(SellProductResponseDto::fromEntity);
    }
    
    /**
     * 카테고리별 판매 가능한 상품 조회
     * 
     * @param category 상품 카테고리
     * @return 카테고리별 판매 가능 상품 목록
     */
    public List<SellProductResponseDto> getProductsByCategory(String category) {
        List<SellProduct> products = sellProductRepository.findByProductCategoryAndProductStatus(
                category, ProductStatus.AVAILABLE);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 판매자별 상품 조회
     * 
     * @param sellerUserIdx 판매자 ID
     * @return 판매자별 상품 목록
     */
    public List<SellProductResponseDto> getProductsBySellerUserIdx(Long sellerUserIdx) {
        List<SellProduct> products = sellProductRepository.findBySellerUserIdx(sellerUserIdx);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 추천 상품 조회
     * 
     * @return 추천 상품 목록
     */
    public List<SellProductResponseDto> getFeaturedProducts() {
        List<SellProduct> products = sellProductRepository.findByFeaturedTrue();
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 태그별 상품 조회
     * 
     * @param tag 검색할 태그
     * @return 태그가 포함된 상품 목록
     */
    public List<SellProductResponseDto> getProductsByTag(String tag) {
        List<SellProduct> products = sellProductRepository.findByProductTag(tag);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 가격 범위로 상품 조회
     * 
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 가격 범위 내 상품 목록
     */
    public List<SellProductResponseDto> getProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        List<SellProduct> products = sellProductRepository.findByPriceRange(minPrice, maxPrice);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 조회수 상위 상품 조회
     * 
     * @return 조회수 기준 상위 5개 상품
     */
    public List<SellProductResponseDto> getTopViewedProducts() {
        List<SellProduct> products = sellProductRepository.findTop5ByOrderByViewCountDesc();
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    /**
     * 추천 상품 상태 토글
     * 추천 상품이면 일반 상품으로, 일반 상품이면 추천 상품으로 상태 변경
     * 
     * @param productId 상품 ID
     * @throws IllegalArgumentException 상품을 찾을 수 없을 경우
     */
    public void toggleFeaturedStatus(Long productId) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setFeatured(!product.getFeatured());
        sellProductRepository.save(product);
    }
}
