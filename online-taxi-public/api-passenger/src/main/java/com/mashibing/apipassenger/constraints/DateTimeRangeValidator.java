package com.mashibing.apipassenger.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeRangeValidator implements ConstraintValidator<DateTimeRange,Object> {

    private String judge;
    private String pattern;

    @Override
    public void initialize(DateTimeRange constraintAnnotation) {
        judge = constraintAnnotation.judge();
        pattern = constraintAnnotation.pattern();
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
            dateValue = LocalDateTime.parse((String)paramDate, DateTimeFormatter.ofPattern(pattern));
        }
        LocalDateTime now = LocalDateTime.now();

        if(judge.equals(DateTimeRange.IS_AFTER)  && dateValue.isAfter(now)){
            return true;
        }
        if(judge.equals(DateTimeRange.IS_BEFORE)  && dateValue.isBefore(now)){
            return true;
        }


        return false;
    }
}
