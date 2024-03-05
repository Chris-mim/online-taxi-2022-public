package com.mashibing.apipassenger.request;

import com.mashibing.apipassenger.constraints.CheckVerificationCodeGroup;
import com.mashibing.apipassenger.constraints.SendVerificationCodeGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class TestSendVerificationCodeDTO {

    @NotBlank(message = "手机号不能为空", groups = {CheckVerificationCodeGroup.class, SendVerificationCodeGroup.class})
    @Pattern(regexp = "^1[3,4,5,6,7,8,9]\\d{9}$", message = "请填写正确的手机号", groups = {CheckVerificationCodeGroup.class, SendVerificationCodeGroup.class})
    private String passengerPhone;

    @NotBlank(message = "验证码不能为空", groups = {CheckVerificationCodeGroup.class})
    @Pattern(regexp = "^\\d{6}$", message = "请填写6位数字的验证码", groups = {CheckVerificationCodeGroup.class})
    private String verificationCode;

}