package com.example.OrderBE.repositories;

import com.example.OrderBE.models.entities.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface  SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE SalesOrder so SET so.status = 2 WHERE so.status = 0 AND so.createdDate < :thirtyMinutesAgo")
    void updateStatusToClosed(@Param("thirtyMinutesAgo") LocalDateTime thirtyMinutesAgo);

}
