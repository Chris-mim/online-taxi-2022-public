package com.mashibing.apipassenger.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class DateTimeRangeValidator implements ConstraintValidator<DateTimeRange,String> {

    private List<String> vehicleTypeCheckValue = null;

    @Override
    public void initialize(DateTimeRange constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        System.out.println("自定义注解校验开始");

        return false;
    }
}
