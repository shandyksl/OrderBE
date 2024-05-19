package com.example.OrderBE.Initializer.cache;

import com.example.OrderBE.models.entities.ProductInfo;
import com.example.OrderBE.repositories.ProductInfoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCacheLoader {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ProductInfoRepository productRepository;

    @Autowired
    public ProductCacheLoader(RedisTemplate<String, Object> redisTemplate, ProductInfoRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void loadProductsIntoCache() {
        List<ProductInfo> products = productRepository.findAll();
        products.forEach(product -> {
            String productKey = product.getProductId();
            if (Boolean.FALSE.equals(redisTemplate.hasKey(productKey))) { // Check if the key already exists
                redisTemplate.opsForHash().put(productKey, "productName", product.getProductName());
                redisTemplate.opsForHash().put(productKey, "quantity", String.valueOf(product.getQuantity()));
            }
        });
    }
}
