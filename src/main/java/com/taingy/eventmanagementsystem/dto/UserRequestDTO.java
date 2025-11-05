package com.taingy.eventmanagementsystem.dto;

import com.taingy.eventmanagementsystem.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String password;
}
