package com.vj.ezybuy.products.service;

import com.vj.ezybuy.products.dto.PagedResponse;
import com.vj.ezybuy.products.dto.ProductDto;
import com.vj.ezybuy.products.dto.ReviewDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    PagedResponse<ProductDto> getAllProducts(int page, int size);

    ProductDto getProductById(UUID productId);

    PagedResponse<ProductDto> getProductsByCategoryId(Long categoryId, int page, int size);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(UUID productId, ProductDto productDto);

    void deleteProduct(UUID productId);

    ProductDto addCategoryToProduct(UUID productId, Long categoryId);

    ProductDto removeCategoryFromProduct(UUID productId, Long categoryId);

    //Add Review to product--> product id ,
    ReviewDto addReviewToProduct(UUID productId, ReviewDto reviewDto);

    //Add product images
    ProductDto addProductImages(UUID productId, List<MultipartFile> files);

    //Get images of product
    List<String> getProductImages(UUID productId);
}
