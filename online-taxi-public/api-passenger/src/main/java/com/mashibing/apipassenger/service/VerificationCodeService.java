package com.mashibing.apipassenger.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.mashibing.apipassenger.remote.ServiceVerificationcodeClient;
import com.mashibing.apipassenger.request.VerificationCodeDTO;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.ResponseResult;
import com.mashibing.internalcommon.response.NumberCodeResponse;
import com.mashibing.internalcommon.response.TokenResponse;
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

    /**
     * 生成验证码
     *
     * @param passengerPhone 手机号
     * @return
     */
    public ResponseResult generatorCode(String passengerPhone) {
        // 调用验证码服务，获取验证码
        ResponseResult<NumberCodeResponse> numberCodeResponse = serviceVerificationcodeClient.getNumberCode(6);
        // 存入Redis
        // key,value,过期时间
        String key = getPhoneRedisKey(passengerPhone);
        String numberCode = numberCodeResponse.getData().getNumberCode() + "";
        stringRedisTemplate.opsForValue().set(key, numberCode, 2, TimeUnit.MINUTES);
        // 通过短信服务商，将对应的验证码发送到手机上。阿里短信服务，腾讯短信通，华信，容联


        return ResponseResult.success();

    }

    private String getPhoneRedisKey(String passengerPhone) {
        return verificationCodePrefix + passengerPhone;
    }

    /**
     * 校验手机号和对于的验证码
     *
     * @param verificationCodeDTO
     * @return
     */
    public ResponseResult checkCode(VerificationCodeDTO verificationCodeDTO) {
        // 去redis 读取验证码
        String key = getPhoneRedisKey(verificationCodeDTO.getPassengerPhone());
        String codeRedis = stringRedisTemplate.opsForValue().get(key);
        // 校验验证码
        if (StringUtils.isBlank(codeRedis)) {
            return ResponseResult.fail(CommonStatusEnum.VERIFICATION_CODE_ERR);
        }
        if (!StringUtils.equals(codeRedis, verificationCodeDTO.getVerificationCode())) {
            return ResponseResult.fail(CommonStatusEnum.VERIFICATION_CODE_ERR);
        }
        // 判断原来是否有用户，并进行对应的处理
        System.out.println("判断原来是否有用户，并进行对应的处理");

        // 颁发令牌
        System.out.println("颁发令牌");

        // 响应
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setToken("token value");
        return ResponseResult.success(tokenResponse);
    }
}
