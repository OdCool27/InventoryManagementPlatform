package org.osyntra.osyntrainventorymanagement.dtos.inventory;

import org.osyntra.osyntrainventorymanagement.dtos.product.ProductResponse;
import java.time.Instant;

public record InventoryResponse(
        ProductResponse product, int quantityOnHand, int quantityReserved, int availableQuantity,
        int reorderLevel, boolean lowStock, Instant updatedAt
) {
}
