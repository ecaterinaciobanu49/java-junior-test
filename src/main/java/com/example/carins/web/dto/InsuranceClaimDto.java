package com.example.carins.web.dto;

import java.util.Date;

public record InsuranceClaimDto(Date claimDate, String description, double amount) {}
