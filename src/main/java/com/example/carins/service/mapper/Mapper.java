package com.example.carins.service.mapper;

import com.example.carins.model.InsuranceClaim;
import com.example.carins.web.dto.InsuranceClaimDto;

public class Mapper {

    public static InsuranceClaim mapToInsuranceClaim(InsuranceClaimDto insuranceClaimDto) {
        InsuranceClaim insuranceClaim = new InsuranceClaim();
        insuranceClaim.setClaimDate(insuranceClaimDto.claimDate());
        insuranceClaim.setDescription(insuranceClaimDto.description());
        insuranceClaim.setAmount(insuranceClaimDto.amount());

        return insuranceClaim;
    }
}
