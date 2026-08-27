package com.vj.ezybuy.products.service.impl;

import com.vj.ezybuy.products.dto.ProductDto;
import com.vj.ezybuy.products.dto.ReviewDto;
import com.vj.ezybuy.products.entity.Product;
import com.vj.ezybuy.products.entity.Review;
import com.vj.ezybuy.products.exception.ResourceNotFoundException;
import com.vj.ezybuy.products.repository.ProductRepository;
import com.vj.ezybuy.products.repository.ReviewRepository;
import com.vj.ezybuy.products.service.ReviewService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ReviewDto getReviewById(Long reviewId) {
        return toDto(findReview(reviewId));
    }

    @Override
    public List<ReviewDto> getReviewsByProductId(UUID productId) {
        return reviewRepository.findByProduct_Id(productId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ReviewDto createReview(UUID productId, ReviewDto reviewDto) {
        Product product = findProduct(productId);
        Review review = new Review();
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setProduct(product);
        return toDto(reviewRepository.save(review));
    }

    @Override
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto) {
        Review review = findReview(reviewId);
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        return toDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = findReview(reviewId);
        reviewRepository.delete(review);
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        if (review.getProduct() != null) {
            dto.setProduct(toProductDto(review.getProduct()));
        }
        return dto;
    }

    private ProductDto toProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setShortDescription(product.getShortDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setLive(product.getLive());
        dto.setProductImages(product.getProductImages() == null ? new ArrayList<>() : new ArrayList<>(product.getProductImages()));
        dto.setCategories(new ArrayList<>());
        dto.setReviews(new ArrayList<>());
        return dto;
    }
}
