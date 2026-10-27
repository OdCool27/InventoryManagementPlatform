package org.osyntra.osyntrainventorymanagement.dtos.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 50) String sku,
        @Size(max = 255) String barcode,
        @NotBlank @Size(max = 120) String itemName,
        @NotBlank @Size(max = 255) String itemDescription,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
        @Positive Integer categoryId,
        @Size(max = 40) String make,
        @Size(max = 40) String model,
        @Pattern(regexp = "[0-9]{4}") String year
) {
    @AssertTrue(message = "make, model and year must all be supplied for a car part")
    public boolean isCompatibilityValid() {
        boolean any = make != null || model != null || year != null;
        return !any || (make != null && !make.isBlank() && model != null && !model.isBlank() && year != null);
    }
}
