package org.osyntra.osyntrainventorymanagement.services;

import org.osyntra.osyntrainventorymanagement.dtos.inventory.*;
import org.osyntra.osyntrainventorymanagement.dtos.product.ProductResponse;
import org.osyntra.osyntrainventorymanagement.models.Inventory;
import org.osyntra.osyntrainventorymanagement.repositories.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductService productService;
    private final AuditService auditService;

    public InventoryService(InventoryRepository inventoryRepository, ProductService productService, AuditService auditService) {
        this.inventoryRepository = inventoryRepository;
        this.productService = productService;
        this.auditService = auditService;
    }

    public InventoryResponse findByProductId(UUID id) {
        return response(productService.findById(id), requireInventory(id, false));
    }

    public List<InventoryResponse> findAll(String search, String category, String make, String model, String year,
                                           boolean lowStock, boolean outOfStock, int limit, int offset) {
        return inventoryRepository.findAll(search, category, make, model, year, lowStock, outOfStock, limit, offset);
    }

    @Transactional
    public InventoryResponse create(UUID id, InventoryRequest request) {
        ProductResponse product = productService.lockActive(id);
        if (inventoryRepository.findByProductId(id, false).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory already exists for this product");
        }
        inventoryRepository.create(id, request);
        auditService.log("INVENTORY_CREATED", "INVENTORY", id, null, request);
        return response(product, requireInventory(id, false));
    }

    @Transactional
    public InventoryResponse changeQuantity(UUID id, int amount, boolean set) {
        ProductResponse product = productService.lockActive(id);
        Inventory previous = requireInventory(id, true);
        long next = set ? amount : (long) previous.getQuantityOnHand() + amount;
        if (next < previous.getQuantityReserved() || next > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient available stock or quantity overflow");
        }
        inventoryRepository.adjust(id, amount, set);
        String action = set ? "QUANTITY_SET" : (amount > 0 ? "STOCK_ADDED" : "STOCK_REMOVED");
        auditService.log(action, "INVENTORY", id, previous.getQuantityOnHand(), next);
        return response(product, requireInventory(id, false));
    }

    @Transactional
    public InventoryResponse setReorderLevel(UUID id, int level) {
        ProductResponse product = productService.lockActive(id);
        Inventory previous = requireInventory(id, true);
        inventoryRepository.setReorderLevel(id, level);
        auditService.log("REORDER_LEVEL_CHANGED", "INVENTORY", id, previous.getReorderLevel(), level);
        return response(product, requireInventory(id, false));
    }

    private Inventory requireInventory(UUID id, boolean lock) {
        return inventoryRepository.findByProductId(id, lock)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));
    }

    private InventoryResponse response(ProductResponse product, Inventory inventory) {
        return new InventoryResponse(product, inventory.getQuantityOnHand(), inventory.getQuantityReserved(),
                inventory.getAvailableQuantity(), inventory.getReorderLevel(), inventory.isLowStock(), inventory.getUpdatedAt());
    }
}
