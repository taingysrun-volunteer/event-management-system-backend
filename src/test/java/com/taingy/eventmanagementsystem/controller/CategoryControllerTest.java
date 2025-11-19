package com.taingy.eventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.eventmanagementsystem.dto.CategoryRequestDTO;
import com.taingy.eventmanagementsystem.dto.CategoryResponseDTO;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.mapper.CategoryMapper;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.taingy.eventmanagementsystem.security.JwtUtil jwtUtil;

    @MockBean
    private com.taingy.eventmanagementsystem.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.taingy.eventmanagementsystem.repository.UserRepository userRepository;

    private Category testCategory;
    private CategoryRequestDTO testCategoryRequestDTO;
    private CategoryResponseDTO testCategoryResponseDTO;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(UUID.randomUUID());
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");
        regularUser.setRole(Role.USER);

        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Technology");
        testCategory.setDescription("Technology related events");

        testCategoryRequestDTO = CategoryRequestDTO.builder()
                .name("Technology")
                .description("Technology related events")
                .build();

        testCategoryResponseDTO = CategoryResponseDTO.builder()
                .id(1)
                .name("Technology")
                .description("Technology related events")
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCategory_Success() throws Exception {
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(categoryMapper.toEntity(any(CategoryRequestDTO.class))).thenReturn(testCategory);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(testCategoryResponseDTO);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description").value("Technology related events"));

        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createCategory_Forbidden_WhenNotAdmin() throws Exception {
        when(authService.getUserByUsername("user")).thenReturn(regularUser);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDTO)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCategory_BadRequest_WhenNameIsEmpty() throws Exception {
        testCategoryRequestDTO.setName("");
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser
    void getAllCategories_Success() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(testCategoryResponseDTO);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].name").value("Technology"));
    }

    @Test
    @WithMockUser
    void getCategoryById_Success() throws Exception {
        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponseDTO(testCategory)).thenReturn(testCategoryResponseDTO);

        mockMvc.perform(get("/api/categories/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Technology"));
    }

    @Test
    @WithMockUser
    void getCategoryById_NotFound() throws Exception {
        when(categoryService.getCategoryById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_Success() throws Exception {
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(testCategory));
        when(categoryService.saveCategory(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(testCategoryResponseDTO);

        mockMvc.perform(put("/api/categories/{id}", 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Technology"));

        verify(categoryService, times(1)).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateCategory_Forbidden_WhenNotAdmin() throws Exception {
        when(authService.getUserByUsername("user")).thenReturn(regularUser);

        mockMvc.perform(put("/api/categories/{id}", 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDTO)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).saveCategory(any(Category.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_NotFound() throws Exception {
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(categoryService.getCategoryById(999)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/categories/{id}", 999)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCategory_Success() throws Exception {
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/categories/{id}", 1)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteCategory_Forbidden_WhenNotAdmin() throws Exception {
        when(authService.getUserByUsername("user")).thenReturn(regularUser);

        mockMvc.perform(delete("/api/categories/{id}", 1)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(anyInt());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCategory_NotFound() throws Exception {
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(categoryService.getCategoryById(999)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/categories/{id}", 999)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
