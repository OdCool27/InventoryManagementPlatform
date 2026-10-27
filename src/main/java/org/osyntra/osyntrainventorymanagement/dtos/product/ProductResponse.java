package org.osyntra.osyntrainventorymanagement.dtos.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID itemId, String sku, String barcode, String itemName, String itemDescription,
        BigDecimal unitPrice, Integer categoryId, String category, boolean active,
        String make, String model, String year, Instant addedAt, Instant modifiedAt
) {
}
