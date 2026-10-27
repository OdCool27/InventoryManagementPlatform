package org.osyntra.osyntrainventorymanagement.services;

import org.osyntra.osyntrainventorymanagement.dtos.inventory.*;
import org.osyntra.osyntrainventorymanagement.repositories.InventoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {
    private final InventoryRepository inventoryRepository;

    public ReportService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventorySummary summary() {
        return inventoryRepository.summary();
    }

    public List<InventoryResponse> lowStock(int limit, int offset) {
        return inventoryRepository.findAll(null, null, null, null, null, true, false, limit, offset);
    }

    public List<InventoryResponse> outOfStock(int limit, int offset) {
        return inventoryRepository.findAll(null, null, null, null, null, false, true, limit, offset);
    }
}
