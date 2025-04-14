package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.SellProduct;
import com.example.AssetTrading.Entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellProductRepository extends JpaRepository<SellProduct, Long> {
    List<SellProduct> findByProductTitleContaining(String keyword);
    List<SellProduct> findByProductStatus(ProductStatus productStatus);
}
