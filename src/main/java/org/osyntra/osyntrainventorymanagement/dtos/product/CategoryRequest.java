package org.osyntra.osyntrainventorymanagement.dtos.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(@NotBlank @Size(max = 60) String name) {
}
