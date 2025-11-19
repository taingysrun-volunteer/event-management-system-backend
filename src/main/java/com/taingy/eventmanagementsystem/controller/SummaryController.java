package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.SummaryResponseDTO;
import com.taingy.eventmanagementsystem.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
@CrossOrigin(origins = "*")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public ResponseEntity<SummaryResponseDTO> getSummary() {
        SummaryResponseDTO summary = summaryService.getSummary();
        return ResponseEntity.ok(summary);
    }
}
