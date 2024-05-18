package com.example.OrderBE.repositories;

import com.example.OrderBE.models.entities.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInfoRepository extends JpaRepository <ProductInfo, Long>{

//    @Query("SELECT s.quantity FROM ProductInfo s WHERE s.productID = :productID")
//    int findQuantityByProductID(@Param("productID") String productID);
}
