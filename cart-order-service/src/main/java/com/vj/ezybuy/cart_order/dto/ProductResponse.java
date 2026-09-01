package com.vj.ezybuy.cart_order.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductResponse {

    private String id;


    private String title;


    private String shortDesc;


    private String longDesc;


    private Double price;


    private Integer discount;

    private Boolean live;
    private List<String> productImages;
}
