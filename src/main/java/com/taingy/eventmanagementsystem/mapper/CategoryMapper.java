package com.taingy.eventmanagementsystem.mapper;

import com.taingy.eventmanagementsystem.dto.CategoryRequestDTO;
import com.taingy.eventmanagementsystem.dto.CategoryResponseDTO;
import com.taingy.eventmanagementsystem.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponseDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public Category toEntity(CategoryRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}
