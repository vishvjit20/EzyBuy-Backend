package com.vj.ezybuy.products.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Lob
    private String longDescription;
    private Double price;
    private Integer discount;
    private Boolean live = false;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> productImages = new ArrayList<>();

//    @ManyToOne
//    private Category category;

    @ManyToMany(mappedBy = "products")
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;




}
