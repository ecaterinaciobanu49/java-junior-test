package com.example.carins.service;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.service.mapper.Mapper;
import com.example.carins.web.dto.InsuranceClaimDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, InsuranceClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null) {
            throw new IllegalArgumentException("Car ID must not be null");
        }

        if (CollectionUtils.isEmpty(policyRepository.findByCarId(carId))) {
            throw new CarNotFoundException("Car with id " + carId + " does not exist");
        }

        return policyRepository.existsActiveOnDate(carId, date);
    }

    public InsuranceClaim createInsuranceClaim(Long carId, InsuranceClaimDto claim) {
        if (carId == null) {
            throw new IllegalArgumentException("Car ID must not be null");
        }

        if (!CollectionUtils.isEmpty(ClaimValidator.validate(claim))) {
            throw new ConstraintViolationException(ClaimValidator.validate(claim));
        }

        Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new CarNotFoundException("Car with id " + carId + " does not exist"));

        InsuranceClaim insuranceClaim = Mapper.mapToInsuranceClaim(claim);
        insuranceClaim.setCar(car);

        return claimRepository.save(insuranceClaim);
    }
}
