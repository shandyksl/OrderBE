package com.example.OrderBE.repositories;

import com.example.OrderBE.models.entities.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  SalesOrderRepostory extends JpaRepository<SalesOrder, Long> {
}
