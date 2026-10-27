package org.osyntra.osyntrainventorymanagement.controllers;

import jakarta.validation.constraints.*;
import org.osyntra.osyntrainventorymanagement.models.AuditLog;
import org.osyntra.osyntrainventorymanagement.services.AuditService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Validated
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditLog> findAll(@RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return auditService.findAll(entityType, entityId, limit, offset);
    }
}
