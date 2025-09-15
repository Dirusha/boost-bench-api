package com.boostbench.api.controller;

import com.boostbench.api.entity.Category;
import com.boostbench.api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<Category> createCategory(@RequestBody @Valid Category category) {
        return new ResponseEntity<>(productService.createCategory(category.getName()), HttpStatus.CREATED);
    }
}