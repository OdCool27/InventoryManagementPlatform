package org.osyntra.osyntrainventorymanagement.dtos.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockRequest(@NotNull @Positive Integer quantity) {
}
