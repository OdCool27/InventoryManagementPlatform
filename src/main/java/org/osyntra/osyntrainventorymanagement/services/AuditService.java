package org.osyntra.osyntrainventorymanagement.services;

import org.osyntra.osyntrainventorymanagement.models.AuditLog;
import org.osyntra.osyntrainventorymanagement.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditService {
    private final AuditRepository auditRepository;
    private final EmployeeRepository employeeRepository;

    public AuditService(AuditRepository auditRepository, EmployeeRepository employeeRepository) {
        this.auditRepository = auditRepository;
        this.employeeRepository = employeeRepository;
    }

    public void log(String action, String entityType, Object entityId, Object oldValue, Object newValue) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        int userId = employeeRepository.findByEmail(email).orElseThrow().getId();
        auditRepository.create(userId, action, entityType, entityId.toString(),
                oldValue == null ? null : oldValue.toString(), newValue == null ? null : newValue.toString());
    }

    public List<AuditLog> findAll(String entityType, String entityId, int limit, int offset) {
        return auditRepository.findAll(entityType, entityId, limit, offset);
    }
}
