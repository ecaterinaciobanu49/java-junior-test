package com.example.carins.service;

import com.example.carins.exception.CarNotFoundException;
import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CarEventDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;


@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    CarRepository carRepository;
    @Mock
    InsurancePolicyRepository policyRepository;
    @Mock
    InsuranceClaimRepository claimRepository;
    @InjectMocks
    CarService carService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void When_CarsExist_ExpectListCars_ReturnListOfCars() {
        Mockito.when(carRepository.findAll()).thenReturn(Collections.singletonList(new Car("4Y1SL65848Z411439",
                "Honda", "Civic", 2021, new Owner("John Doe", "john.doe@mail.example.com"))));

        List<Car> cars = carService.listCars();
        Assertions.assertEquals(1, cars.size());
    }

    @Test
    void When_CarHasActivePolicyOnDate_ExpectIsInsuranceValid_ReturnTrue() {
        Mockito.when(policyRepository.findByCarId(anyLong())).thenReturn(Collections.singletonList(new InsurancePolicy(new Car("4Y1SL65848Z411439",
                "Honda", "Civic", 2021, new Owner("John Doe", "john.doe@mail.example.com")), "SRL INSURANCE",
                LocalDate.parse("2024-02-01"), LocalDate.parse("2025-02-02"))));
        Mockito.when(policyRepository.existsActiveOnDate(anyLong(), any())).thenReturn(true);

        boolean isValid = carService.isInsuranceValid(1L, LocalDate.parse("2024-03-03"));
        Assertions.assertTrue(isValid);
    }

    @Test
    void When_CarDoesNotExist_ExpectIsInsuranceValid_ThrowCarNotFoundException() {
        Mockito.when(policyRepository.findByCarId(anyLong())).thenReturn(Collections.emptyList());

        Assertions.assertThrows(CarNotFoundException.class, () -> carService.isInsuranceValid(1L, LocalDate.parse("2024-03-03")));
    }

    @Test
    void When_CarIdIsNull_ExpectIsInsuranceValid_ThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> carService.isInsuranceValid(null, LocalDate.parse("2024-03-03")));
    }

    @Test
    void When_DateIsTooEarly_ExpectIsInsuranceValid_ThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> carService.isInsuranceValid(1L, LocalDate.parse("1899-03-03")));
    }

    @Test
    void When_DateIsTooLate_ExpectIsInsuranceValid_ThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> carService.isInsuranceValid(1L, LocalDate.parse("2500-03-03")));
    }

    @Test
    void When_DateIsNull_ExpectIsInsuranceValid_ThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> carService.isInsuranceValid(1L, null));
    }


    @Test
    void When_ValidClaimForExistingCar_ExpectCreateInsuranceClaim_PersistAndReturnClaim() {
        Mockito.when(carRepository.findById(anyLong())).thenReturn(Optional.of(new Car("4Y1SL65848Z411439",
                "Honda", "Civic", 2021, new Owner("John Doe", "john.doe@mail.example.com"))));
        Mockito.when(claimRepository.save(any())).thenReturn(new InsuranceClaim());
        InsuranceClaimDto insuranceClaimDto = new InsuranceClaimDto(new Date(), "ACCIDENT", 20000);

        Assertions.assertNotNull(carService.createInsuranceClaim(1L, insuranceClaimDto));
    }

    @Test
    void When_CarIdIsNull_ExpectExpectCreateInsuranceClaim_ThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> carService.createInsuranceClaim(null, new InsuranceClaimDto(new Date(), "ACCIDENT", 20000)));
    }

    @Test
    void When_InsuranceClaimDtoNotValid_ExpectExpectCreateInsuranceClaim_ThrowConstraintViolationException() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> carService
                .createInsuranceClaim(1L, new InsuranceClaimDto(null, "ACCIDENT", 20000)));
    }

    @Test
    void When_CarIdIsNull_ExpectGetCarEvents_ThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> carService.getCarEvents(null));
    }

    @Test
    void When_CarDoesNotExist_ExpectGetCarEvents_ThrowCarNotFoundException() {
        Assertions.assertThrows(CarNotFoundException.class, () -> carService.getCarEvents(1L));
    }

    @Test
    void When_CarExistsButHasNoClaimsOrPolicies_Expect_ReturnEmptyList() {
        Mockito.when(carRepository.existsById(anyLong())).thenReturn(true);

        List<CarEventDto> events = carService.getCarEvents(1L);
        Assertions.assertEquals(0, events.size());
    }

    @Test
    void When_CarHasPoliciesWithStartAndEndDates_Expect_ReturnBothPolicyEvents() {
        Mockito.when(carRepository.existsById(anyLong())).thenReturn(true);
        Mockito.when(policyRepository.findAllByCarIdOrderByStartDate(anyLong())).thenReturn(Collections
                .singletonList(new InsurancePolicy(new Car("4Y1SL65848Z411439",
                        "Honda", "Civic", 2021, new Owner("John Doe", "john.doe@mail.example.com")), "SRL INSURANCE",
                        LocalDate.parse("2024-02-01"), LocalDate.parse("2025-02-02"))));

        Mockito.when(claimRepository.findAllByCarIdOrderByDate(anyLong())).thenReturn(Collections.singletonList(new InsuranceClaim(new Car("4Y1SL65848Z411439",
                "Honda", "Civic", 2021, new Owner("John Doe", "john.doe@mail.example.com")), new Date(), "INSURANCE", 15000)));

        List<CarEventDto> events = carService.getCarEvents(1L);
        Assertions.assertEquals(3, events.size());
    }
}