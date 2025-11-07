package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.dto.UserRequestDTO;
import com.taingy.eventmanagementsystem.dto.UserResponseDTO;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.exception.ForbiddenException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.exception.UnauthorizedException;
import com.taingy.eventmanagementsystem.mapper.UserMapper;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.UserService;
import com.taingy.eventmanagementsystem.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthService authService;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper, AuthService authService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponseDTO(createdUser));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userService.getAllUsers(search, role, pageable);

        List<UserResponseDTO> users = userPage.getContent().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("pageSize", userPage.getSize());
        response.put("hasNext", userPage.hasNext());
        response.put("hasPrevious", userPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponseDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id, @RequestBody AuthRequests.ResetPasswordRequest request) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can reset user passwords");
        }

        userService.resetUserPassword(id, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
