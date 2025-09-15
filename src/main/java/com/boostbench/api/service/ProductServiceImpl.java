package com.boostbench.api.service;

import com.boostbench.api.dto.ProductDto;
import com.boostbench.api.entity.Category;
import com.boostbench.api.entity.Product;
import com.boostbench.api.entity.Tag;
import com.boostbench.api.exception.ResourceNotFoundException;
import com.boostbench.api.repository.CategoryRepository;
import com.boostbench.api.repository.ProductRepository;
import com.boostbench.api.repository.TagRepository;
import com.boostbench.api.util.FileUploadUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public List<ProductDto> getAllProducts(String period, boolean specialOffers, boolean bestsellers, List<Long> categoryIds, List<Long> tagIds, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products;

        if (period != null && !period.isEmpty()) {
            // Handle period-based filtering (new arrivals)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;
            switch (period.toLowerCase()) {
                case "week":
                    startDate = now.minus(1, ChronoUnit.WEEKS);
                    break;
                case "month":
                    startDate = now.minus(1, ChronoUnit.MONTHS);
                    break;
                case "year":
                    startDate = now.minus(1, ChronoUnit.YEARS);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid period: " + period + ". Supported: week, month, year.");
            }
            products = productRepository.findByCreatedAtAfter(startDate);
        } else {
            // If no period is specified, get all products
            products = productRepository.findAll();
        }

        // Apply special offers filter if requested
        if (specialOffers) {
            products = products.stream()
                    .filter(product -> product.getDiscount() != null && product.getDiscount().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());
        }

        // Apply bestsellers filter if requested
        if (bestsellers) {
            products = products.stream()
                    .filter(product -> {
                        if (product.getQuantity() == null || product.getQuantity() == 0) {
                            return false; // Avoid division by zero
                        }
                        double soldPercentage = (double) product.getSoldQuantity() / product.getQuantity() * 100;
                        return soldPercentage > 60.0; // Bestseller threshold: 60%
                    })
                    .collect(Collectors.toList());
        }

        // Apply category filter if requested
        if (categoryIds != null && !categoryIds.isEmpty()) {
            products = products.stream()
                    .filter(product -> product.getCategories().stream()
                            .anyMatch(category -> categoryIds.contains(category.getId())))
                    .collect(Collectors.toList());
        }

        // Apply tag filter if requested
        if (tagIds != null && !tagIds.isEmpty()) {
            products = products.stream()
                    .filter(product -> product.getTags().stream()
                            .anyMatch(tag -> tagIds.contains(tag.getId())))
                    .collect(Collectors.toList());
        }

        // Apply price range filter if requested
        if (minPrice != null || maxPrice != null) {
            products = products.stream()
                    .filter(product -> {
                        BigDecimal price = product.getPrice();
                        boolean matchesMin = minPrice == null || price.compareTo(minPrice) >= 0;
                        boolean matchesMax = maxPrice == null || price.compareTo(maxPrice) <= 0;
                        return matchesMin && matchesMax;
                    })
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return convertToDto(product);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto, List<MultipartFile> images) {
        try {
            Product product = convertToEntity(productDto);

            // Set categories and tags
            List<Category> categories = productDto.getCategoryIds() != null ?
                    categoryRepository.findAllById(productDto.getCategoryIds()) : new ArrayList<>();
            List<Tag> tags = productDto.getTagIds() != null ?
                    tagRepository.findAllById(productDto.getTagIds()) : new ArrayList<>();

            product.setCategories(categories);
            product.setTags(tags);

            // Handle image uploads
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        try {
                            String imageUrl = fileUploadUtil.uploadFile(image);
                            imageUrls.add(imageUrl);
                            System.out.println("Uploaded image: " + imageUrl);
                        } catch (IOException e) {
                            System.err.println("Failed to upload image: " + e.getMessage());
                            throw new RuntimeException("Failed to upload image: " + image.getOriginalFilename(), e);
                        }
                    }
                }
            }

            product.setImageUrls(imageUrls);

            // Set available quantity if not provided
            if (product.getAvailableQuantity() == null) {
                product.setAvailableQuantity(product.getQuantity());
            }

            Product savedProduct = productRepository.save(product);
            System.out.println("Product saved with " + savedProduct.getImageUrls().size() + " images");

            return convertToDto(savedProduct);

        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            throw new RuntimeException("Failed to create product: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto, List<MultipartFile> images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        // Update basic fields
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setQuantity(productDto.getQuantity());
        product.setAvailableQuantity(productDto.getAvailableQuantity());
        product.setDiscount(productDto.getDiscount());
        product.setColor(productDto.getColor());
        product.setSku(productDto.getSku());

        // Update categories and tags
        List<Category> categories = productDto.getCategoryIds() != null ?
                categoryRepository.findAllById(productDto.getCategoryIds()) : new ArrayList<>();
        List<Tag> tags = productDto.getTagIds() != null ?
                tagRepository.findAllById(productDto.getTagIds()) : new ArrayList<>();

        product.setCategories(categories);
        product.setTags(tags);

        // Handle image updates
        if (images != null && !images.isEmpty()) {
            List<String> existingImageUrls = product.getImageUrls();
            List<String> newImageUrls = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                if (!image.isEmpty()) {
                    try {
                        String imageUrl;
                        if (i < existingImageUrls.size()) {
                            imageUrl = fileUploadUtil.updateFile(image, existingImageUrls.get(i));
                        } else {
                            imageUrl = fileUploadUtil.uploadFile(image);
                        }
                        newImageUrls.add(imageUrl);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to update image: " + image.getOriginalFilename(), e);
                    }
                }
            }

            product.setImageUrls(newImageUrls);
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        productRepository.delete(product);
    }

    @Override
    public Category createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalStateException("Category with name " + name + " already exists");
        }
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    public Tag createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new IllegalStateException("Tag with name " + name + " already exists");
        }
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public List<ProductDto> getNewArrivals(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        switch (period.toLowerCase()) {
            case "week":
                startDate = now.minus(1, ChronoUnit.WEEKS);
                break;
            case "month":
                startDate = now.minus(1, ChronoUnit.MONTHS);
                break;
            case "year":
                startDate = now.minus(1, ChronoUnit.YEARS);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period + ". Supported: week, month, year.");
        }
        List<Product> products = productRepository.findByCreatedAtAfter(startDate);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        // Map category and tag IDs
        productDto.setCategoryIds(product.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList()));
        productDto.setTagIds(product.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toList()));

        // Ensure image URLs are properly set with full URLs
        List<String> fullImageUrls = product.getImageUrls().stream()
                .map(this::getFullImageUrl)
                .collect(Collectors.toList());
        productDto.setImageUrls(fullImageUrls);

        // Set timestamps
        productDto.setCreatedAt(product.getCreatedAt());
        productDto.setUpdatedAt(product.getUpdatedAt());

        System.out.println("Converting product to DTO - Image URLs: " + productDto.getImageUrls());

        return productDto;
    }

    private Product convertToEntity(ProductDto productDto) {
        Product product = modelMapper.map(productDto, Product.class);
        // Clear the collections to avoid issues with ModelMapper
        product.setCategories(new ArrayList<>());
        product.setTags(new ArrayList<>());
        product.setImageUrls(new ArrayList<>());
        return product;
    }

    private String getFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return relativePath;
        }

        // If it's already a full URL, return as is
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }

        // If it starts with /uploads/, create full URL
        if (relativePath.startsWith("/uploads/")) {
            return baseUrl + relativePath;
        }

        // If it doesn't start with /, add it
        if (!relativePath.startsWith("/")) {
            return baseUrl + "/uploads/" + relativePath;
        }

        return baseUrl + relativePath;
    }
}