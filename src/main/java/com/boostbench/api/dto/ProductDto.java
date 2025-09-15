package com.boostbench.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDto {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity must be at least 0")
    private Integer availableQuantity;

    @DecimalMin(value = "0.0", message = "Discount must be at least 0")
    private BigDecimal discount;

    @Size(max = 50, message = "Color must be at most 50 characters")
    private String color;

    @Size(max = 50, message = "SKU must be at most 50 characters")
    private String sku;

    private List<Long> categoryIds;

    private List<Long> tagIds;

    private List<String> imageUrls;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}