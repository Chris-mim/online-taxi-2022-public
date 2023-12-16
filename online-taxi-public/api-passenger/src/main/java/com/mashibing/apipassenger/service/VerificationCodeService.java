package com.mashibing.apipassenger.service;

import com.mashibing.apipassenger.remote.ServiceVerificationcodeClient;
import com.mashibing.internalcommon.constant.ResponseResult;
import com.mashibing.internalcommon.response.NumberCodeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {

    @Autowired
    private ServiceVerificationcodeClient serviceVerificationcodeClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 乘客验证码的前缀
    private String verificationCodePrefix = "passenger-verification-code-";

    public ResponseResult generatorCode(String passengerPhone) {
        // 调用验证码服务，获取验证码
        ResponseResult<NumberCodeResponse> numberCodeResponse = serviceVerificationcodeClient.getNumberCode(6);
        // 存入Redis
        // key,value,过期时间
        String key = verificationCodePrefix + passengerPhone;
        String numberCode = numberCodeResponse.getData().getNumberCode() + "";
        stringRedisTemplate.opsForValue().set(key, numberCode, 2, TimeUnit.MINUTES);
        // 通过短信服务商，将对应的验证码发送到手机上。阿里短信服务，腾讯短信通，华信，容联


        return ResponseResult.success();

    }

}
