package com.boostbench.api.controller;

import com.boostbench.api.dto.TagDto;
import com.boostbench.api.entity.Tag;
import com.boostbench.api.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.getAllTags()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(convertToDto(tag));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TAG_CREATE')")
    public ResponseEntity<TagDto> createTag(@RequestBody @Valid TagDto tagDto) {
        Tag tag = tagService.createTag(tagDto.getName());
        return new ResponseEntity<>(convertToDto(tag), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TAG_UPDATE')")
    public ResponseEntity<TagDto> updateTag(@PathVariable Long id, @RequestBody @Valid TagDto tagDto) {
        Tag tag = tagService.updateTag(id, tagDto.getName());
        return ResponseEntity.ok(convertToDto(tag));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TAG_DELETE')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    private TagDto convertToDto(Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        return dto;
    }
}