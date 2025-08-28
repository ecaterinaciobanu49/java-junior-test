package com.example.carins.service.mapper;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import com.example.carins.web.dto.InsurancePolicyDto;

public class Mapper {

    public static InsuranceClaim mapToInsuranceClaim(InsuranceClaimDto insuranceClaimDto) {
        InsuranceClaim insuranceClaim = new InsuranceClaim();
        insuranceClaim.setClaimDate(insuranceClaimDto.claimDate());
        insuranceClaim.setDescription(insuranceClaimDto.description());
        insuranceClaim.setAmount(insuranceClaimDto.amount());

        return insuranceClaim;
    }

    public static InsurancePolicy mapToInsurancePolicy(InsurancePolicyDto insurancePolicyDto) {
        InsurancePolicy insurancePolicy = new InsurancePolicy();
        insurancePolicy.setProvider(insurancePolicyDto.provider());
        insurancePolicy.setEndDate(insurancePolicyDto.endDate());
        insurancePolicy.setStartDate(insurancePolicyDto.startDate());

        return insurancePolicy;
    }

    public static Car mapToCar(CarDto carDto) {
        Car car = new Car();
        car.setMake(carDto.make());
        car.setModel(carDto.model());
        car.setVin(carDto.vin());
        car.setYearOfManufacture(carDto.year());

        return car;
    }
}
