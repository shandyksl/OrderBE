package com.example.OrderBE.repositories;
import com.example.OrderBE.models.entities.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInfoRepository extends JpaRepository <ProductInfo, Long>{
    ProductInfo findByProductId(String productId);
}
