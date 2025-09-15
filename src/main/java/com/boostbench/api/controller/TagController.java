package com.boostbench.api.controller;

import com.boostbench.api.entity.Tag;
import com.boostbench.api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(productService.getAllTags());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TAG_CREATE')")
    public ResponseEntity<Tag> createTag(@RequestBody @Valid Tag tag) {
        return new ResponseEntity<>(productService.createTag(tag.getName()), HttpStatus.CREATED);
    }
}