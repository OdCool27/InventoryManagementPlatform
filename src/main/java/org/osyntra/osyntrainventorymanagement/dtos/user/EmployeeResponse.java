package org.osyntra.osyntrainventorymanagement.dtos.user;

import org.osyntra.osyntrainventorymanagement.models.EmployeeRole;
import org.osyntra.osyntrainventorymanagement.models.UserStatus;

public record EmployeeResponse(Integer id, String employeeId, String firstName, String lastName,
                               String email, String jobTitle, EmployeeRole role, UserStatus status) {
}
