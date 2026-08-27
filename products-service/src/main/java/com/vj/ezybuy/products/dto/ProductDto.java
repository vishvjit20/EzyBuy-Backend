package com.vj.ezybuy.products.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private UUID id;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "Short Description is required")
    @Size(max = 500, message = "Short Description must be at most 500 characters")
    private String shortDescription;

    @NotBlank(message = "Long Description is required")
    private String longDescription;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;


    @Min(value = 0, message = "Discount must be greater than or equal to 0")
    @Max(value = 100, message = "Discount must be less than or equal to 100")
    private Integer discount;

    private Boolean live;

    private List<String> productImages;
    private List<CategoryDto> categories;
    private List<ReviewDto> reviews;

}
