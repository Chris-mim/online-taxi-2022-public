package com.mashibing.apipassenger.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 只针对字段
@Retention(RetentionPolicy.RUNTIME) // 运行时执行
@Constraint(validatedBy = VehicleTypeCheckValidator.class) // 自定义约束注解, 指定了该约束注解所对应的校验器。
public @interface VehicleTypeCheck {
    /**
     * 车辆类型的选项
     * @return
     */
    String[] vehicleTypeValue() default {};

    /**
     * 提示信息
     * @return
     */
    String message() default "车辆类型不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
