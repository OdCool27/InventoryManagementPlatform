package org.osyntra.osyntrainventorymanagement.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.osyntra.osyntrainventorymanagement.dtos.inventory.*;
import org.osyntra.osyntrainventorymanagement.services.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> findAll(@RequestParam(required = false) String search,
            @RequestParam(required = false) String category, @RequestParam(required = false) String make,
            @RequestParam(required = false) String model, @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "false") boolean lowStock,
            @RequestParam(defaultValue = "false") boolean outOfStock,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return inventoryService.findAll(search, category, make, model, year, lowStock, outOfStock, limit, offset);
    }

    @GetMapping("/{productId}")
    public InventoryResponse findByProductId(@PathVariable UUID productId) {
        return inventoryService.findByProductId(productId);
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@PathVariable UUID productId, @Valid @RequestBody InventoryRequest request) {
        return inventoryService.create(productId, request);
    }

    @PutMapping("/{productId}/quantity")
    public InventoryResponse setQuantity(@PathVariable UUID productId, @Valid @RequestBody QuantityRequest request) {
        return inventoryService.changeQuantity(productId, request.quantity(), true);
    }

    @PostMapping("/{productId}/add")
    public InventoryResponse addStock(@PathVariable UUID productId, @Valid @RequestBody StockRequest request) {
        return inventoryService.changeQuantity(productId, request.quantity(), false);
    }

    @PostMapping("/{productId}/remove")
    public InventoryResponse removeStock(@PathVariable UUID productId, @Valid @RequestBody StockRequest request) {
        return inventoryService.changeQuantity(productId, -request.quantity(), false);
    }

    @PutMapping("/{productId}/reorder-level")
    public InventoryResponse setReorderLevel(@PathVariable UUID productId, @Valid @RequestBody ReorderRequest request) {
        return inventoryService.setReorderLevel(productId, request.reorderLevel());
    }
}
