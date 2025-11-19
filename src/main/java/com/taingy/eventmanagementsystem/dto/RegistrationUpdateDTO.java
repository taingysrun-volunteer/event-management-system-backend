package com.taingy.eventmanagementsystem.dto;

import com.taingy.eventmanagementsystem.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationUpdateDTO {
    private RegistrationStatus status;
    private String note;
}
