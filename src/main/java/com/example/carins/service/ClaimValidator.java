package com.example.carins.service;

import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.InsuranceClaimDto;
import com.example.carins.web.dto.InsurancePolicyDto;
import jakarta.validation.*;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.*;

import java.util.Set;

public class ClaimValidator {

    public static Set<ConstraintViolation<InsuranceClaimDto>> validate(InsuranceClaimDto dto) {

        HibernateValidatorConfiguration configuration = Validation
                .byProvider(HibernateValidator.class)
                .configure();

        ConstraintMapping mapping = configuration.createConstraintMapping();
        mapping
                .type(InsuranceClaimDto.class)
                .field("claimDate")
                .constraint(new NotNullDef())
                .field("amount")
                .constraint(new PositiveDef());

        Validator customValidator = configuration
                .addMapping(mapping)
                .buildValidatorFactory()
                .getValidator();

        return customValidator.validate(dto);
    }

    public static Set<ConstraintViolation<InsurancePolicyDto>> validatePolicy(InsurancePolicyDto dto) {

        HibernateValidatorConfiguration configuration = Validation
                .byProvider(HibernateValidator.class)
                .configure();

        ConstraintMapping mapping = configuration.createConstraintMapping();
        mapping
                .type(InsurancePolicyDto.class)
                .field("endDate")
                .constraint(new FutureDef())
                .field("startDate")
                .constraint(new PastOrPresentDef())
                .field("provider")
                .constraint(new NotNullDef());

        Validator customValidator = configuration
                .addMapping(mapping)
                .buildValidatorFactory()
                .getValidator();

        return customValidator.validate(dto);
    }

    public static Set<ConstraintViolation<CarDto>> validateCar(CarDto dto) {

        HibernateValidatorConfiguration configuration = Validation
                .byProvider(HibernateValidator.class)
                .configure();

        ConstraintMapping mapping = configuration.createConstraintMapping();
        mapping
                .type(CarDto.class)
                .field("vin")
                .constraint(new NotNullDef())
                .constraint(new SizeDef().min(5).max(32))
                .field("make")
                .constraint(new NotNullDef())
                .field("model")
                .constraint(new NotNullDef())
                .field("year")
                .constraint(new NotNullDef())
                .constraint(new MinDef().value(1991));

        Validator customValidator = configuration
                .addMapping(mapping)
                .buildValidatorFactory()
                .getValidator();

        return customValidator.validate(dto);
    }
}
