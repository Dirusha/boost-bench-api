package com.boostbench.api.service;

import com.boostbench.api.dto.ProductDto;
import com.boostbench.api.entity.Category;
import com.boostbench.api.entity.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    List<ProductDto> getAllProducts(String period, boolean specialOffers, boolean bestsellers, List<Long> categoryIds, List<Long> tagIds, BigDecimal minPrice, BigDecimal maxPrice);

    ProductDto getProductById(Long id);

    ProductDto createProduct(ProductDto productDto, List<MultipartFile> images);

    ProductDto updateProduct(Long id, ProductDto productDto, List<MultipartFile> images);

    void deleteProduct(Long id);

    Category createCategory(String name);

    Tag createTag(String name);

    List<Category> getAllCategories();

    List<Tag> getAllTags();

    List<ProductDto> getNewArrivals(String period);
}