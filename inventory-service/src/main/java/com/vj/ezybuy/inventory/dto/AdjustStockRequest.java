package com.vj.ezybuy.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull Integer quantityDelta,
        String reason) {
}