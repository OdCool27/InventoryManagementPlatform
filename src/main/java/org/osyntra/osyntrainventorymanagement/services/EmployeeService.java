package org.osyntra.osyntrainventorymanagement.services;

import org.osyntra.osyntrainventorymanagement.dtos.user.*;
import org.osyntra.osyntrainventorymanagement.models.Employee;
import org.osyntra.osyntrainventorymanagement.repositories.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        Employee employee = new Employee(request.firstName().trim(), request.lastName().trim(),
                request.email().trim().toLowerCase(Locale.ROOT), passwordEncoder.encode(request.password()),
                null, request.jobTitle().trim(), request.role());
        employeeRepository.save(employee);
        auditService.log("EMPLOYEE_CREATED", "EMPLOYEE", employee.getId(), null, employee.getRole());
        return response(employee);
    }

    public EmployeeResponse findByEmail(String email) {
        return response(employeeRepository.findByEmail(email).orElseThrow());
    }

    private EmployeeResponse response(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getEmpId(), employee.getFirstName(),
                employee.getLastName(), employee.getEmail(), employee.getJobTitle(), employee.getRole(), employee.getStatus());
    }
}
