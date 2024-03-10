package com.mashibing.apipassenger.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeRangeValidator implements ConstraintValidator<DateTimeRange,Object> {

    /**
     * 会自动注入注解
     */
    private DateTimeRange dateTimeRange;

    @Override
    public void initialize(DateTimeRange constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object paramDate, ConstraintValidatorContext constraintValidatorContext) {

        LocalDateTime dateValue = null;
        if (paramDate == null){
            return true;
        }
        if(paramDate instanceof LocalDateTime){
            dateValue = (LocalDateTime) paramDate;
        }
        if (paramDate instanceof String){
            dateValue = LocalDateTime.parse((String)paramDate, DateTimeFormatter.ofPattern(dateTimeRange.pattern()));
        }

        LocalDateTime now = LocalDateTime.now();

        if(dateValue.isAfter(now)){
            return true;
        }


        return false;
    }
}
