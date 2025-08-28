package com.example.carins.service;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.exception.OwnerNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.service.mapper.Mapper;
import com.example.carins.service.utils.DateUtils;
import com.example.carins.web.dto.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.example.carins.service.ValidationMessages.CAR_ID_REQUIRED;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;
    private final OwnerRepository ownerRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, InsuranceClaimRepository claimRepository, OwnerRepository ownerRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.ownerRepository = ownerRepository;
    }

    public InsurancePolicy insertNewInsurancePolicy(Long carId, InsurancePolicyDto insurancePolicyDto) {
        if (carId == null) {
            throw new IllegalArgumentException(CAR_ID_REQUIRED);
        }
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(carId));

        if (!CollectionUtils.isEmpty(ClaimValidator.validatePolicy(insurancePolicyDto))) {
            throw new ConstraintViolationException(ClaimValidator.validatePolicy(insurancePolicyDto));
        }

        InsurancePolicy insurancePolicy = Mapper.mapToInsurancePolicy(insurancePolicyDto);
        insurancePolicy.setCar(car);

        return policyRepository.save(insurancePolicy);
    }

    public Car addNewCar(CarDto  carDto) {
        if (!CollectionUtils.isEmpty(ClaimValidator.validateCar(carDto))) {
            throw new ConstraintViolationException(ClaimValidator.validateCar(carDto));
        }

        Owner owner = ownerRepository.findById(carDto.ownerId())
                .orElseThrow(() -> new OwnerNotFoundException(carDto.ownerId()));

        Car car = Mapper.mapToCar(carDto);
        car.setOwner(owner);

        return carRepository.save(car);
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        DateUtils.checkDateErrors(date);
        if (carId == null) {
            throw new IllegalArgumentException(CAR_ID_REQUIRED);
        }

        if (CollectionUtils.isEmpty(policyRepository.findByCarId(carId))) {
            throw new CarNotFoundException(carId);
        }

        return policyRepository.existsActiveOnDate(carId, date);
    }

    public InsuranceClaim createInsuranceClaim(Long carId, InsuranceClaimDto claim) {
        if (carId == null) {
            throw new IllegalArgumentException(CAR_ID_REQUIRED);
        }

        if (!CollectionUtils.isEmpty(ClaimValidator.validate(claim))) {
            throw new ConstraintViolationException(ClaimValidator.validate(claim));
        }

        Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new CarNotFoundException(carId));

        InsuranceClaim insuranceClaim = Mapper.mapToInsuranceClaim(claim);
        insuranceClaim.setCar(car);

        return claimRepository.save(insuranceClaim);
    }

    public List<CarEventDto> getCarEvents(Long carId) {
        if (carId == null) {
            throw new IllegalArgumentException(CAR_ID_REQUIRED);
        }
        if (!carRepository.existsById(carId)) {
            throw new CarNotFoundException(carId);
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
