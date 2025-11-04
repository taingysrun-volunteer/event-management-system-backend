package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.CategoryRequestDTO;
import com.taingy.eventmanagementsystem.dto.CategoryResponseDTO;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.ForbiddenException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.exception.UnauthorizedException;
import com.taingy.eventmanagementsystem.mapper.CategoryMapper;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.CategoryService;
import com.taingy.eventmanagementsystem.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final AuthService authService;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper, AuthService authService) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        if (categoryRequestDTO.getName() == null || categoryRequestDTO.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can create categories");
        }

        Category category = categoryMapper.toEntity(categoryRequestDTO);
        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponseDTO(savedCategory));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories().stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return ResponseEntity.ok(categoryMapper.toResponseDTO(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Integer id, @RequestBody CategoryRequestDTO categoryRequestDTO) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can update categories");
        }

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(categoryRequestDTO.getName());
        category.setDescription(categoryRequestDTO.getDescription());
        Category updated = categoryService.saveCategory(category);
        return ResponseEntity.ok(categoryMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can delete categories");
        }

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        categoryService.deleteCategory(category.getId());
        return ResponseEntity.noContent().build();
    }
}

