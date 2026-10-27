package org.osyntra.osyntrainventorymanagement.dtos.user;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {
}
