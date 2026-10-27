package org.osyntra.osyntrainventorymanagement.dtos.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequest(@NotNull @Min(0) Integer quantityOnHand, @NotNull @Min(0) Integer reorderLevel) {
}
