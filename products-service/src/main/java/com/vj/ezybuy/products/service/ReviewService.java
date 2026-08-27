package com.vj.ezybuy.products.service;

import com.vj.ezybuy.products.dto.ReviewDto;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    List<ReviewDto> getAllReviews();

    ReviewDto getReviewById(Long reviewId);

    List<ReviewDto> getReviewsByProductId(UUID productId);

    ReviewDto createReview(UUID productId, ReviewDto reviewDto);

    ReviewDto updateReview(Long reviewId, ReviewDto reviewDto);

    void deleteReview(Long reviewId);
}
