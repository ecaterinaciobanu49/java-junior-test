package com.example.carins.web.dto;

import java.time.LocalDate;

public record InsurancePolicyDto (String provider, LocalDate startDate, LocalDate endDate) {
}
