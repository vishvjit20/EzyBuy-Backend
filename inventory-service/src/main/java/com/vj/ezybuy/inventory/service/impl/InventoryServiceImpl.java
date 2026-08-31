package com.vj.ezybuy.inventory.service.impl;

import com.vj.ezybuy.common.payload.ProductSnapshot;
import com.vj.ezybuy.inventory.domain.InventoryItem;
import com.vj.ezybuy.inventory.dto.*;
import com.vj.ezybuy.inventory.exception.BusinessRuleException;
import com.vj.ezybuy.inventory.exception.ResourceNotFoundException;
import com.vj.ezybuy.inventory.external.ProductClient;
import com.vj.ezybuy.inventory.repository.InventoryRepository;
import com.vj.ezybuy.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final ProductClient productClient;

    @Override
    public InventoryResponse create(CreateInventoryRequest request) {

        // Check if product already exists
        ProductSnapshot productSnapshot = null;
        try {
            productSnapshot = productClient
                    .getProductById(request.productId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Product not found");
        }

        // Business rules
        String sku = normalizeSku(request.sku());
        // Check if sku already exists
        if (repository.existsBySku(sku)) {
            throw new BusinessRuleException("Inventory already exists for sku: " + sku);
        }

        // Check if inventory exists by product
        if (repository.existsByProductId(request.productId())) {
            throw new BusinessRuleException("Inventory already exists for productId: " + request.productId());
        }

        InventoryItem item = new InventoryItem();
        item.setProductId(request.productId());
        item.setSku(sku);
        item.setProductName(trim(productSnapshot.title()));
        item.setWarehouseLocation(trim(request.warehouseLocation()));
        item.setAvailableQuantity(defaultZero(request.availableQuantity()));
        item.setReservedQuantity(defaultZero(request.reservedQuantity()));
        item.setReorderLevel(defaultZero(request.reorderLevel()));
        item.setActive(request.active() == null || request.active());

        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse update(Long id, UpdateInventoryRequest request) {
        InventoryItem item = findEntity(id);
        item.setProductName(trim(request.productName()));
        item.setWarehouseLocation(trim(request.warehouseLocation()));
        item.setReorderLevel(request.reorderLevel());
        item.setActive(request.active());
        return toResponse(repository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getBySku(String sku) {
        return toResponse(repository.findBySku(normalizeSku(sku))
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for sku: " + sku)));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        return toResponse(repository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId: " + productId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock(int threshold) {
        return repository.findByAvailableQuantityLessThanEqualAndActiveTrueOrderByAvailableQuantityAsc(threshold)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InventoryResponse adjustStock(Long id, AdjustStockRequest request) {
        InventoryItem item = findEntityForUpdate(id);
        int delta = request.quantityDelta();
        int nextAvailable = safeInt(item.getAvailableQuantity()) + delta;
        if (nextAvailable < 0) {
            throw new BusinessRuleException("Adjustment would make available quantity negative");
        }
        item.setAvailableQuantity(nextAvailable);
        item.setReasonToAdjustQuantity(trim(request.reason()));
        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse reserveStock(Long id, ReserveStockRequest request) {
        InventoryItem item = findEntityForUpdate(id);
        int quantity = request.quantity();
        int available = safeInt(item.getAvailableQuantity());
        if (available < quantity) {
            throw new BusinessRuleException("Insufficient available stock to reserve");
        }
        item.setAvailableQuantity(available - quantity);
        item.setReservedQuantity(safeInt(item.getReservedQuantity()) + quantity);
        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse releaseStock(Long id, ReleaseStockRequest request) {
        InventoryItem item = findEntityForUpdate(id);
        int quantity = request.quantity();
        int reserved = safeInt(item.getReservedQuantity());
        if (reserved < quantity) {
            throw new BusinessRuleException("Insufficient reserved stock to release");
        }
        item.setReservedQuantity(reserved - quantity);
        item.setAvailableQuantity(safeInt(item.getAvailableQuantity()) + quantity);
        return toResponse(repository.save(item));
    }

    @Override
    public InventoryResponse reserveStockByProductId(UUID productId, ReserveStockRequest request) {
        InventoryItem item = repository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId: " + productId));
        return reserve(item, request.quantity());
    }

    @Override
    public InventoryResponse releaseStockByProductId(UUID productId, ReleaseStockRequest request) {
        InventoryItem item = repository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId: " + productId));
        return release(item, request.quantity());
    }

    @Override
    public void delete(Long id) {
        InventoryItem item = findEntity(id);
        repository.delete(item);
    }

    private InventoryItem findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));
    }

    private InventoryItem findEntityForUpdate(Long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for id: " + id));
    }

    private InventoryResponse reserve(InventoryItem item, int quantity) {
        int available = safeInt(item.getAvailableQuantity());
        if (available < quantity) {
            throw new BusinessRuleException("Insufficient available stock to reserve");
        }
        item.setAvailableQuantity(available - quantity);
        item.setReservedQuantity(safeInt(item.getReservedQuantity()) + quantity);
        return toResponse(repository.save(item));
    }

    private InventoryResponse release(InventoryItem item, int quantity) {
        int reserved = safeInt(item.getReservedQuantity());
        if (reserved < quantity) {
            throw new BusinessRuleException("Insufficient reserved stock to release");
        }
        item.setReservedQuantity(reserved - quantity);
        item.setAvailableQuantity(safeInt(item.getAvailableQuantity()) + quantity);
        return toResponse(repository.save(item));
    }

    private InventoryResponse toResponse(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getWarehouseLocation(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getReorderLevel(),
                item.isActive(),
                item.getTotalQuantity(),
                item.isLowStock(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }


    //checks for sku contains text:
    private String normalizeSku(String sku) {
        //sku normaize rules
        if (!StringUtils.hasText(sku)) {
            throw new BusinessRuleException("SKU is required");
        }
//		IPHONE-14-BLACK
        return sku.trim().toUpperCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
