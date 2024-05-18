package com.example.OrderBE.scheduler;

import com.example.OrderBE.models.entities.SalesOrder;
import com.example.OrderBE.repositories.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatusScheduler {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Scheduled(fixedRate = 60000) // Runs every minute
    public void updateOrderStatus() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        salesOrderRepository.updateStatusToClosed(thirtyMinutesAgo);
    }
}
