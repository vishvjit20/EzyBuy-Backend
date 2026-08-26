package com.vj.ezybuy.products.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private String title;
    private String shortDescription;
    private String longDescription;
    private Double price;
    private Integer discount;
    private Boolean live;
    private List<String> productImages;
    private List<Long> categoryIds;

}
