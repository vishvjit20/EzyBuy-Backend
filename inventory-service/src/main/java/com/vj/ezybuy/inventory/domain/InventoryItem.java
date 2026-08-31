package com.vj.ezybuy.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_sku", columnNames = "sku"),
        @UniqueConstraint(name = "uk_inventory_product_id", columnNames = "productId")
})
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, length = 128, unique = true)
    private String sku;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 120)
    private String warehouseLocation;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    // threshold -> refill -> 5
    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String reasonToAdjustQuantity;

    // executed before saving the entity
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (availableQuantity == null) {
            availableQuantity = 0;
        }
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        if (reorderLevel == null) {
            reorderLevel = 0;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    public int getTotalQuantity() {
        return safeInt(availableQuantity) + safeInt(reservedQuantity);
    }

    public boolean isLowStock() {
        return safeInt(availableQuantity) <= safeInt(reorderLevel);
    }

}
