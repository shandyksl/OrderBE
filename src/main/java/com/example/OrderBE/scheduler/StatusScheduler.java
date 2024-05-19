package com.example.OrderBE.scheduler;

import com.example.OrderBE.models.entities.ProductInfo;
import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.ProductInfoRepository;
import com.example.OrderBE.repositories.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatusScheduler {

    private final SalesOrderRepository salesOrderRepository;

    private final ProductInfoRepository productInfoRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public StatusScheduler(SalesOrderRepository salesOrderRepository,ProductInfoRepository productInfoRepository,RedisTemplate<String, Object> redisTemplate) {
        this.salesOrderRepository = salesOrderRepository;
        this.productInfoRepository = productInfoRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 60000) // Runs every minute
    public void updateOrderStatus() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        List<SalesOrder> expiredOrders = salesOrderRepository.findExpiredOrders(thirtyMinutesAgo);

        for (SalesOrder salesorder : expiredOrders) {
            // Status Open : 0, Paid : 1, Closed : 2
            salesorder.setStatus(2);
            salesOrderRepository.save(salesorder);

            // 恢复Redis中的库存
            redisTemplate.opsForHash().increment(salesorder.getProductId(), "quantity", salesorder.getQuantity());

            // 恢复 MySQL 数据库中的库存
            ProductInfo product = productInfoRepository.findByProductId(salesorder.getProductId());
            product.setQuantity(product.getQuantity() + salesorder.getQuantity());
            productInfoRepository.save(product);
        }
    }
}
