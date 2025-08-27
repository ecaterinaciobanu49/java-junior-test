package com.example.carins.service;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.service.mapper.Mapper;
import com.example.carins.web.dto.CarEventDto;
import com.example.carins.web.dto.CarEventType;
import com.example.carins.web.dto.InsuranceClaimDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<CarEventDto> getCarEvents(Long carId) {
        if (carId == null) {
            throw new IllegalArgumentException("Car ID must not be null");
        }
        if (!carRepository.existsById(carId)) {
            throw new CarNotFoundException("Car with id " + carId + " does not exist");
        }
        List<CarEventDto> claimEvents = claimRepository.findAllByCarIdOrderByDate(carId).stream()
                .map(c -> new CarEventDto(
                        CarEventType.CLAIM,
                        c.getClaimDate().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate(),
                        "Insurance claim",
                        c.getDescription(),
                        c.getId()
                ))
                .toList();

        List<CarEventDto> policyEvents = policyRepository.findAllByCarIdOrderByStartDate(carId).stream()
                .flatMap(p -> {
                    List<CarEventDto> list = new java.util.ArrayList<>();
                    if (p.getStartDate() != null) {
                        list.add(new CarEventDto(
                                CarEventType.POLICY_START,
                                p.getStartDate(),
                                "Policy started",
                                p.getProvider(),
                                p.getId()
                        ));
                    }
                    if (p.getEndDate() != null) {
                        list.add(new CarEventDto(
                                CarEventType.POLICY_END,
                                p.getEndDate(),
                                "Policy ended",
                                p.getProvider(),
                                p.getId()
                        ));
                    }
                    return list.stream();
                })
                .toList();

        List<CarEventDto> allEvents = new java.util.ArrayList<>(claimEvents.size() + policyEvents.size());
        allEvents.addAll(claimEvents);
        allEvents.addAll(policyEvents);
        allEvents.sort(java.util.Comparator
                .comparing(CarEventDto::date)
                .thenComparing(e -> e.type().name()));

        return allEvents;
    }
}
