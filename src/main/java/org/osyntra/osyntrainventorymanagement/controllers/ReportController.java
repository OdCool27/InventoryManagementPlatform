package org.osyntra.osyntrainventorymanagement.controllers;

import jakarta.validation.constraints.*;
import org.osyntra.osyntrainventorymanagement.dtos.inventory.*;
import org.osyntra.osyntrainventorymanagement.services.ReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public InventorySummary summary() {
        return reportService.summary();
    }

    @GetMapping("/low-stock")
    public List<InventoryResponse> lowStock(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
                                           @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return reportService.lowStock(limit, offset);
    }

    @GetMapping("/out-of-stock")
    public List<InventoryResponse> outOfStock(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
                                             @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return reportService.outOfStock(limit, offset);
    }
}
