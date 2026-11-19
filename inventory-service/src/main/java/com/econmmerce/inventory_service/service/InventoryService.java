package com.econmmerce.inventory_service.service;


import com.econmmerce.inventory_service.dto.InventoryRequest;
import com.econmmerce.inventory_service.dto.InventoryResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface InventoryService {
    boolean isInStock(String sku, Integer quantity);
    InventoryResponse createInventory(InventoryRequest inventoryRequest);
    List<InventoryResponse> getAllInventory();
    InventoryResponse updateInventory(Long id, InventoryRequest inventoryRequest);
    void deleteInventory(Long id);
    ResponseEntity<Void> reduceStock(String sku, Integer quantity);
}