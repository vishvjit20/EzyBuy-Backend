package com.vj.ezybuy.products.service.impl;

import com.vj.ezybuy.products.dto.CategoryDto;
import com.vj.ezybuy.products.dto.PagedResponse;
import com.vj.ezybuy.products.dto.ProductDto;
import com.vj.ezybuy.products.dto.ReviewDto;
import com.vj.ezybuy.products.entity.Category;
import com.vj.ezybuy.products.entity.Product;
import com.vj.ezybuy.products.entity.Review;
import com.vj.ezybuy.products.exception.InvalidRequestException;
import com.vj.ezybuy.products.exception.ResourceNotFoundException;
import com.vj.ezybuy.products.repository.CategoryRepository;
import com.vj.ezybuy.products.repository.ProductRepository;
import com.vj.ezybuy.products.repository.ReviewRepository;
import com.vj.ezybuy.products.service.ImageStorageService;
import com.vj.ezybuy.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ImageStorageService imageStorageService;

    @Override
    public PagedResponse<ProductDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        return toPagedResponse(productPage.map(this::toDto));
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return toDto(findProduct(productId));
    }

    @Override
    public PagedResponse<ProductDto> getProductsByCategoryId(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategories_Id(categoryId, pageable);
        return toPagedResponse(productPage.map(this::toDto));
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product  product = new Product();
        applyBasicFields(product, productDto);
        List<Category> categories = resolveCategories(productDto.getCategories());
        product.setCategories(categories);
        Product savedProduct = productRepository.save(product);
        syncCategoryLinks(savedProduct, categories);
        return toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(UUID productId, ProductDto productDto) {
        Product product = findProduct(productId);
        applyBasicFields(product, productDto);
        if (productDto.getCategories() != null) {
            List<Category> categories = resolveCategories(productDto.getCategories());
            product.setCategories(categories);

            Product savedProduct = productRepository.save(product);
            syncCategoryLinks(savedProduct, categories);
            return toDto(savedProduct);
        }
        return toDto(productRepository.save(product));
    }

    @Override
    public void deleteProduct(UUID productId) {
        Product product = findProduct(productId);
        productRepository.delete(product);
    }

    @Override
    public ProductDto addCategoryToProduct(UUID productId, Long categoryId) {
        Product product = findProduct(productId);
        Category category = findCategory(categoryId);
        if (!product.getCategories().contains(category)) {
            product.getCategories().add(category);
        }
        if (!category.getProducts().contains(product)) {
            category.getProducts().add(product);
        }
        categoryRepository.save(category);
        return toDto(productRepository.save(product));
    }

    @Override
    public ProductDto removeCategoryFromProduct(UUID productId, Long categoryId) {
        Product product = findProduct(productId);
        Category category = findCategory(categoryId);

        //1step
        product.getCategories().remove(category);
        //2step
        category.getProducts().remove(product);

        categoryRepository.save(category);
        return toDto(productRepository.save(product));
    }

    @Override
    public ReviewDto addReviewToProduct(UUID productId, ReviewDto reviewDto) {
        Product product = findProduct(productId);
        Review review = new Review();
        review.setTitle(reviewDto.getTitle());
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setProduct(product);
        return toReviewDto(reviewRepository.save(review));
    }

    @Override
    public ProductDto addProductImages(UUID productId, List<MultipartFile> files) {
        Product product = findProduct(productId);

        //will upload the images:
        List<String> uploadedUrls = uploadImages(files);


        if (product.getProductImages() == null) {
            product.setProductImages(new ArrayList<>());
        }
        product.getProductImages().addAll(uploadedUrls);
        return toDto(productRepository.save(product));
    }

    @Override
    public List<String> getProductImages(UUID productId) {
        return List.of();
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private void applyBasicFields(Product product, ProductDto productDto) {
        product.setTitle(productDto.getTitle());
        product.setShortDescription(productDto.getShortDescription());
        product.setLongDescription(productDto.getLongDescription());
        product.setPrice(productDto.getPrice());
        product.setDiscount(productDto.getDiscount());
        if (productDto.getLive() != null) {
            product.setLive(productDto.getLive());
        }
        if (productDto.getProductImages() != null) {
            product.setProductImages(new ArrayList<>(productDto.getProductImages()));
        }
    }

    private List<Category> resolveCategories(List<CategoryDto> categoryDtos) {
        if (categoryDtos == null) {
            return new ArrayList<>();
        }

        List<Category> categories = new ArrayList<>();
        for (CategoryDto categoryDto : categoryDtos) {
            if (categoryDto.getId() == null) {
                Category category = new Category();
                category.setTitle(categoryDto.getTitle());
                categories.add(categoryRepository.save(category));
            } else {
                categories.add(findCategory(categoryDto.getId()));
            }
        }
        return categories;
    }

    private void syncCategoryLinks(Product product, List<Category> categories) {
        for (Category category : categories) {
            if (!category.getProducts().contains(product)) {
                category.getProducts().add(product);
            }
            categoryRepository.save(category);
        }
    }

    private List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new InvalidRequestException("At least one product image is required");
        }
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedUrls.add(imageStorageService.upload(file));
        }
        return uploadedUrls;
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setShortDescription(product.getShortDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setLive(product.getLive());
        dto.setProductImages(product.getProductImages() == null ? new ArrayList<>() : new ArrayList<>(product.getProductImages()));
        dto.setCategories(product.getCategories() == null ? new ArrayList<>() : product.getCategories().stream().map(this::toCategoryDtoShallow).collect(Collectors.toList()));
        dto.setReviews(product.getReviews() == null ? new ArrayList<>() : product.getReviews().stream().map(this::toReviewDtoShallow).collect(Collectors.toList()));

        return dto;
    }

    private CategoryDto toCategoryDtoShallow(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
//        dto.setProducts(new ArrayList<>());
        return dto;
    }

    private PagedResponse<ProductDto> toPagedResponse(Page<ProductDto> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast()
        );
    }

    private ReviewDto toReviewDtoShallow(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setProduct(null);
        return dto;
    }

    private ReviewDto toReviewDto(Review review) {
        ReviewDto dto = toReviewDtoShallow(review);
        if (review.getProduct() != null) {
            dto.setProduct(toProductDtoShallow(review.getProduct()));
        }
        return dto;
    }

    private ProductDto toProductDtoShallow(Product product) {
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
