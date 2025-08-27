package com.example.carins.web.dto;

import java.time.LocalDate;

public record CarEventDto(
        CarEventType type,
        LocalDate date,
        String title,
        String details,
        Long sourceId
) {
}