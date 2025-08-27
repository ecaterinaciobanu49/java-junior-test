package com.example.carins.web;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.CarEventDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        try {
            LocalDate d = LocalDate.parse(date);
            boolean valid = service.isInsuranceValid(carId, d);
            return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Provided date format " + date + " is invalid");
        } catch (IllegalArgumentException | CarNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<?> getCarEvents(@PathVariable Long carId) {
       try {
           List<CarEventDto> events = service.getCarEvents(carId);
           return ResponseEntity.ok(new CarEventResponse(carId, events));
       } catch (CarNotFoundException e) {
           return ResponseEntity.notFound().build();
       }
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<?> addNewInsuranceClaim(@PathVariable Long carId, @RequestBody InsuranceClaimDto insuranceClaimDto) {
        try {
            InsuranceClaim insuranceClaim = service.createInsuranceClaim(carId, insuranceClaimDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new InsuranceClaimInsertionResponse(carId, insuranceClaim));
        } catch (IllegalArgumentException | CarNotFoundException | ConstraintViolationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {
    }

    public record InsuranceClaimInsertionResponse(Long carId, InsuranceClaim insuranceClaimDto) {
    }

    public record CarEventResponse(Long carId, List<CarEventDto> carEvents) {
    }
}
