package com.taingy.eventmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponseDTO {
    private Long totalEvents;
    private Long totalUsers;
    private Long totalCategories;
}
