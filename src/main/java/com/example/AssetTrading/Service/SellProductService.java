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
import lombok.RequiredArgsConstructor;
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
        if (!(userObj instanceof UserResponseDto)) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }

        UserResponseDto userDto = (UserResponseDto) userObj;

        // 실제 User 엔티티를 조회
        User user = userRepository.findByUserId(userDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        dto.setSellerUserId(user.getUserId());
        return createSellProduct(dto);
    }

    public List<SellProductResponseDto> getAllSellProducts(String keyword) {
        List<SellProduct> products = sellProductRepository.findByProductTitleContaining(keyword);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }

    public SellProductResponseDto getSellProductById(Long productId) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return SellProductResponseDto.fromEntity(product);
    }

    public void deleteSellProduct(Long productId) {
        sellProductRepository.deleteById(productId);
    }

    public void updateStatus(Long productId, boolean isSellingAvailable) {
        SellProduct product = sellProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setProductStatus(isSellingAvailable ? ProductStatus.AVAILABLE : ProductStatus.UNAVAILABLE);
        sellProductRepository.save(product);
    }

    public List<SellProductResponseDto> getVisibleProducts() {
        List<SellProduct> products = sellProductRepository.findByProductStatus(ProductStatus.AVAILABLE);
        return products.stream().map(SellProductResponseDto::fromEntity).collect(Collectors.toList());
    }
}
