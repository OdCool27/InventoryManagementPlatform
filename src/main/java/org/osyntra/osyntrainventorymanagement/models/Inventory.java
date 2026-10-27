package org.osyntra.osyntrainventorymanagement.models;

import java.time.Instant;
import java.util.UUID;

public class Inventory {
    private UUID productId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer reorderLevel;
    private Instant updatedAt;

    //CONSTRUCTORS
    public Inventory(UUID productId, Integer quantityOnHand, Integer quantityReserved, Integer reorderLevel, Instant updatedAt) {
        this.productId = productId;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
        this.reorderLevel = reorderLevel;
        this.updatedAt = updatedAt;
    }

    //ACCESSORS
    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public Integer getQuantityReserved() {
        return quantityReserved;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    public boolean isLowStock() {
        return getAvailableQuantity() <= reorderLevel;
    }

}
