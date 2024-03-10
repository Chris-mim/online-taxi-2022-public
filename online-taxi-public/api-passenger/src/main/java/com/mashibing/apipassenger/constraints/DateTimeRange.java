package com.mashibing.apipassenger.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateTimeRangeValidator.class)
public @interface DateTimeRange {

    String pattern() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 在当前时间之后：isAfter
     * 在当前时间之前：isBefore
     * @return
     */
    String judge() default "isAfter";

    String message() default "日期不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
