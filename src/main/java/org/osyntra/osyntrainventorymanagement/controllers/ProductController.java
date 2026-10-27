package org.osyntra.osyntrainventorymanagement.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.osyntra.osyntrainventorymanagement.dtos.product.*;
import org.osyntra.osyntrainventorymanagement.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> findAll(@RequestParam(required = false) String search,
            @RequestParam(required = false) String category, @RequestParam(required = false) String make,
            @RequestParam(required = false) String model, @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return productService.findAll(search, category, make, model, year, includeInactive, limit, offset);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
    }
}
