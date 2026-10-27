package org.osyntra.osyntrainventorymanagement.controllers;

import jakarta.validation.Valid;
import org.osyntra.osyntrainventorymanagement.dtos.user.*;
import org.osyntra.osyntrainventorymanagement.services.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.create(request);
    }
}
