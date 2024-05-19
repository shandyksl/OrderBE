package com.example.OrderBE.repositories;

import com.example.OrderBE.models.entities.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface  SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Query("SELECT o FROM SalesOrder o WHERE o.status = 0 AND o.createdDate <= :thirtyMinutesAgo")
    List<SalesOrder> findExpiredOrders(@Param("thirtyMinutesAgo") LocalDateTime thirtyMinutesAgo);

}
