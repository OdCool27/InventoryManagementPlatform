package org.osyntra.osyntrainventorymanagement.dtos.user;

import jakarta.validation.constraints.*;
import org.osyntra.osyntrainventorymanagement.models.EmployeeRole;

public record CreateEmployeeRequest(
        @NotBlank @Size(max = 30) String firstName,
        @NotBlank @Size(max = 30) String lastName,
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotBlank @Size(max = 100) String jobTitle,
        @NotNull EmployeeRole role
) {
    @AssertTrue(message = "password must be at most 72 UTF-8 bytes")
    public boolean isPasswordLengthValid() {
        return password == null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
    }
}
