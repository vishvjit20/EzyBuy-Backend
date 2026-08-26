package com.vj.ezybuy.products.repository;

import com.vj.ezybuy.products.entity.Product;
import com.vj.ezybuy.products.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);

    List<Review> findByProduct_Id(Long productId);
}
