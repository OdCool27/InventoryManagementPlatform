package org.osyntra.osyntrainventorymanagement.dtos.inventory;

public record InventorySummary(long totalProducts, long stockedProducts, long totalUnits,
                               long lowStockProducts, long outOfStockProducts) {
}
