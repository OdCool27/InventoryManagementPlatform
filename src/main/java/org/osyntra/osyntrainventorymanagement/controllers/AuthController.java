package org.osyntra.osyntrainventorymanagement.controllers;

import org.osyntra.osyntrainventorymanagement.dtos.user.EmployeeResponse;
import org.osyntra.osyntrainventorymanagement.services.EmployeeService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final EmployeeService employeeService;

    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/me")
    public EmployeeResponse currentEmployee(Principal principal) {
        return employeeService.findByEmail(principal.getName());
    }
}
