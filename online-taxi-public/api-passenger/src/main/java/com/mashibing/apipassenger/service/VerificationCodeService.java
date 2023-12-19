package com.mashibing.apipassenger.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.mashibing.apipassenger.remote.ServicePassengerUserClient;
import com.mashibing.apipassenger.remote.ServiceVerificationcodeClient;
import com.mashibing.internalcommon.constant.IdentityConstants;
import com.mashibing.internalcommon.constant.TokenConstants;
import com.mashibing.internalcommon.request.VerificationCodeDTO;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.NumberCodeResponse;
import com.mashibing.internalcommon.response.TokenResponse;
import com.mashibing.internalcommon.util.JwtUtils;
import com.mashibing.internalcommon.util.RedisPrefixUtils;
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
    @Autowired
    private ServicePassengerUserClient servicePassengerUserClient;



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
        String key = RedisPrefixUtils.getPhoneKey(passengerPhone);
        String numberCode = numberCodeResponse.getData().getNumberCode() + "";
        stringRedisTemplate.opsForValue().set(key, numberCode, 2, TimeUnit.MINUTES);
        // 通过短信服务商，将对应的验证码发送到手机上。阿里短信服务，腾讯短信通，华信，容联


        return ResponseResult.success();

    }



    /**
     * 校验手机号和对于的验证码
     *
     * @param verificationCodeDTO 封装到一个对象里，固然方便扩展添加参数，但是对于几乎不怎么改动的方法参数仍然没必要强行传对象，
     *                            或者对于有很多地方调用的，调用起来还要封装一个对象，会不太方便
     * @return
     */
    public ResponseResult checkCode(VerificationCodeDTO verificationCodeDTO) {
        // 去redis 读取验证码
        String key = RedisPrefixUtils.getPhoneKey(verificationCodeDTO.getPassengerPhone());
        String codeRedis = stringRedisTemplate.opsForValue().get(key);
        // 校验验证码
        if (StringUtils.isBlank(codeRedis)) {
            return ResponseResult.fail(CommonStatusEnum.VERIFICATION_CODE_ERR);
        }
        if (!StringUtils.equals(codeRedis, verificationCodeDTO.getVerificationCode())) {
            return ResponseResult.fail(CommonStatusEnum.VERIFICATION_CODE_ERR);
        }
        // 判断原来是否有用户，并进行对应的处理
        servicePassengerUserClient.loginOrRegister(verificationCodeDTO);

        // 颁发令牌
        String accessToken = JwtUtils.generatorToken(verificationCodeDTO.getPassengerPhone(), IdentityConstants.PASSENGER_IDENTITY, TokenConstants.ACCESS_TOKEN_TYPE);
        String refreshToken = JwtUtils.generatorToken(verificationCodeDTO.getPassengerPhone(), IdentityConstants.PASSENGER_IDENTITY, TokenConstants.REFRESH_TOKEN_TYPE);
        // 将token存到redis当中
        String accessTokenKey = RedisPrefixUtils.generatorTokenKey(verificationCodeDTO.getPassengerPhone(), IdentityConstants.PASSENGER_IDENTITY, TokenConstants.ACCESS_TOKEN_TYPE);
        String refreshTokenKey = RedisPrefixUtils.generatorTokenKey(verificationCodeDTO.getPassengerPhone(), IdentityConstants.PASSENGER_IDENTITY, TokenConstants.REFRESH_TOKEN_TYPE);
        stringRedisTemplate.opsForValue().set(accessTokenKey, accessToken, 30, TimeUnit.DAYS);
        stringRedisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 31, TimeUnit.DAYS);

        // 响应
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        return ResponseResult.success(tokenResponse);
    }
}
