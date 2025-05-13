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

@Service
@RequiredArgsConstructor
public class SellProductService {

    private final SellProductRepository sellProductRepository;
    private final UserRepository userRepository;

    public SellProductResponseDto createSellProduct(SellProductRequestDto dto) {
        SellProduct product = dto.toEntity();
        product.setProductStatus(ProductStatus.AVAILABLE);
        SellProduct saved = sellProductRepository.save(product);
        return SellProductResponseDto.fromEntity(saved);
    }

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

    public List<SellProductResponseDto> getAllSellProducts(String keyword) {
        List<SellProduct> products = sellProductRepository.findByProductTitleContaining(keyword);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public Page<SellProductResponseDto> getAllSellProductsPaged(String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("DESC") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SellProduct> productPage = sellProductRepository.findByProductTitleContaining(keyword, pageable);
        return productPage.map(SellProductResponseDto::fromEntity);
    }

    @Transactional
    public SellProductResponseDto getSellProductById(Long productId) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        // 조회수 증가
        sellProductRepository.incrementViewCount(productId);
        
        return SellProductResponseDto.fromEntity(product);
    }

    public void deleteSellProduct(Long productId) {
        sellProductRepository.deleteById(productId);
    }

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
        
        if (dto.getFeatured() != null) {
            product.setFeatured(dto.getFeatured());
        }
        
        SellProduct updated = sellProductRepository.save(product);
        return SellProductResponseDto.fromEntity(updated);
    }

    public void updateStatus(Long productId, boolean isSellingAvailable) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setProductStatus(isSellingAvailable ? ProductStatus.AVAILABLE : ProductStatus.UNAVAILABLE);
        sellProductRepository.save(product);
    }
    
    public void updateProductStatus(Long productId, String status) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        try {
            ProductStatus newStatus = ProductStatus.valueOf(status.toUpperCase());
            product.setProductStatus(newStatus);
            sellProductRepository.save(product);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상품 상태입니다: " + status);
        }
    }

    public List<SellProductResponseDto> getVisibleProducts() {
        List<SellProduct> products = sellProductRepository.findByProductStatus(ProductStatus.AVAILABLE);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public Page<SellProductResponseDto> getVisibleProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SellProduct> productPage = sellProductRepository.findByProductStatus(ProductStatus.AVAILABLE, pageable);
        return productPage.map(SellProductResponseDto::fromEntity);
    }
    
    public List<SellProductResponseDto> getProductsByCategory(String category) {
        List<SellProduct> products = sellProductRepository.findByProductCategoryAndProductStatus(
                category, ProductStatus.AVAILABLE);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public List<SellProductResponseDto> getProductsBySellerUserIdx(Long sellerUserIdx) {
        List<SellProduct> products = sellProductRepository.findBySellerUserIdx(sellerUserIdx);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public List<SellProductResponseDto> getFeaturedProducts() {
        List<SellProduct> products = sellProductRepository.findByFeaturedTrue();
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public List<SellProductResponseDto> getProductsByTag(String tag) {
        List<SellProduct> products = sellProductRepository.findByProductTag(tag);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public List<SellProductResponseDto> getProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        List<SellProduct> products = sellProductRepository.findByPriceRange(minPrice, maxPrice);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public List<SellProductResponseDto> getTopViewedProducts() {
        List<SellProduct> products = sellProductRepository.findTop5ByOrderByViewCountDesc();
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
    
    public void toggleFeaturedStatus(Long productId) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setFeatured(!product.getFeatured());
        sellProductRepository.save(product);
    }
}
