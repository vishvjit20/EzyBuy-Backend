package com.vj.ezybuy.products.repository;

import com.vj.ezybuy.products.entity.Product;
import com.vj.ezybuy.products.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);

    List<Review> findByProduct_Id(UUID productId);
}
