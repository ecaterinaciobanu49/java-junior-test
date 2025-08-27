package com.example.carins.service;

import com.example.carins.web.dto.InsuranceClaimDto;
import jakarta.validation.*;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.PositiveDef;

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
}
