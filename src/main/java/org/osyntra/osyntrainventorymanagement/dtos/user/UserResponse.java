package org.osyntra.osyntrainventorymanagement.dtos.user;

import org.osyntra.osyntrainventorymanagement.models.UserStatus;

import java.time.Instant;

public record UserResponse(
        String firstName,
        String lastName,
        String email,
        UserStatus status,
        Instant createdAt,
        Instant modifiedAt
) {
}
