package com.boostbench.api.controller;

import com.boostbench.api.dto.ProductDto;
import com.boostbench.api.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) String period,
            @RequestParam(required = false, defaultValue = "false") boolean specialOffers,
            @RequestParam(required = false, defaultValue = "false") boolean bestsellers,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        System.out.println("Fetching products" +
                (period != null ? " for period: " + period : "") +
                (specialOffers ? " with special offers" : "") +
                (bestsellers ? " with bestsellers" : "") +
                (categoryIds != null && !categoryIds.isEmpty() ? " with categoryIds: " + categoryIds : "") +
                (tagIds != null && !tagIds.isEmpty() ? " with tagIds: " + tagIds : "") +
                (minPrice != null ? " with minPrice: " + minPrice : "") +
                (maxPrice != null ? " with maxPrice: " + maxPrice : ""));
        return ResponseEntity.ok(productService.getAllProducts(period, specialOffers, bestsellers, categoryIds, tagIds, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        System.out.println("Fetching product with id: " + id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductDto> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            System.out.println("Received product JSON: " + productJson);

            // Parse the JSON string to ProductDto
            ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);

            System.out.println("Creating product: " + productDto.getName());
            return new ResponseEntity<>(productService.createProduct(productDto, images), HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("Error parsing product JSON: " + e.getMessage());
            throw new IllegalArgumentException("Invalid product data: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            System.out.println("Received product JSON for update: " + productJson);

            // Parse the JSON string to ProductDto
            ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);

            System.out.println("Updating product with id: " + id);
            return ResponseEntity.ok(productService.updateProduct(id, productDto, images));

        } catch (Exception e) {
            System.err.println("Error parsing product JSON: " + e.getMessage());
            throw new IllegalArgumentException("Invalid product data: " + e.getMessage());
        }
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductDto> createProductJson(@Valid @RequestBody ProductDto productDto) {
        System.out.println("Creating product via JSON: " + productDto.getName());
        return new ResponseEntity<>(productService.createProduct(productDto, null), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductDto> updateProductJson(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        System.out.println("Updating product via JSON with id: " + id);
        return ResponseEntity.ok(productService.updateProduct(id, productDto, null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        System.out.println("Deleting product with id: " + id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}