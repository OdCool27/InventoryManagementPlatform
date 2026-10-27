package org.osyntra.osyntrainventorymanagement.controllers;

import jakarta.validation.Valid;
import org.osyntra.osyntrainventorymanagement.dtos.product.*;
import org.osyntra.osyntrainventorymanagement.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return productService.categories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return productService.createCategory(request);
    }
}
