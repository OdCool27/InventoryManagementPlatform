package org.osyntra.osyntrainventorymanagement.dtos.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReorderRequest(@NotNull @Min(0) Integer reorderLevel) {
}
