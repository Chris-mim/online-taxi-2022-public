package com.mashibing.apipassenger.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

// <VehicleTypeCheck,String> : 指定加注解的字段是String类型
public class VehicleTypeCheckValidator implements ConstraintValidator<VehicleTypeCheck,String> {
    private List<String> vehicleTypeCheckValue = null;

    @Override
    public boolean isValid(String o, ConstraintValidatorContext constraintValidatorContext) {
        return vehicleTypeCheckValue.contains(o);
    }

    @Override
    public void initialize(VehicleTypeCheck constraintAnnotation) {
        vehicleTypeCheckValue = Arrays.asList(constraintAnnotation.vehicleTypeValue());
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
