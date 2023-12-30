package com.mashibing.apidriver.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.mashibing.apidriver.remote.ServiceDriverUserClient;
import com.mashibing.apidriver.remote.ServiceVerificationcodeClient;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.DriverCarConstants;
import com.mashibing.internalcommon.constant.IdentityConstants;
import com.mashibing.internalcommon.constant.TokenConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.VerificationCodeDTO;
import com.mashibing.internalcommon.response.DriverUserExistsResponse;
import com.mashibing.internalcommon.response.NumberCodeResponse;
import com.mashibing.internalcommon.response.TokenResponse;
import com.mashibing.internalcommon.util.JwtUtils;
import com.mashibing.internalcommon.util.RedisPrefixUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerificationCodeService {


    @Autowired
    private ServiceDriverUserClient serviceDriverUserClient;
    @Autowired
    private ServiceVerificationcodeClient serviceVerificationcodeClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成验证码
     *
     * @param driverPhone 手机号
     * @return
     */
    public ResponseResult generatorCode(String driverPhone) {
        // 1、验证码司机手机号是否存在
        log.info("手机号：" + driverPhone);
        ResponseResult<DriverUserExistsResponse> driverUserExistsResponse = serviceDriverUserClient.getUser(driverPhone);

        DriverUserExistsResponse driverUser = driverUserExistsResponse.getData();
        if (driverUser.getIfExists() == DriverCarConstants.DRIVER_NOT_EXISTS) {
            return ResponseResult.fail(CommonStatusEnum.DRIVER_NOT_EXISTS);
        }
        log.info("司机存在:" + driverPhone);
        // 2、调用验证码服务，获取验证码
        ResponseResult<NumberCodeResponse> numberCodeResponse = serviceVerificationcodeClient.getNumberCode(6);
        int numberCode = numberCodeResponse.getData().getNumberCode();
        log.info("验证码：" + numberCode);
        // 3、存入Redis
        // key,value,过期时间
        String phoneKey = RedisPrefixUtils.getPhoneKey(driverPhone, IdentityConstants.DRIVER_IDENTITY);
        stringRedisTemplate.opsForValue().set(phoneKey, numberCode+"", 2, TimeUnit.MINUTES);

        // 4、第三方调用，通过短信服务商，将对应的验证码发送到手机上。阿里短信服务，腾讯短信通，华信，容联


        return ResponseResult.success();

    }

    /**
     * 校验手机号和对于的验证码
     *
     * @param verificationCodeDTO 封装到一个对象里，固然方便扩展添加参数，但是对于几乎不怎么改动的方法参数仍然没必要强行传对象，
     *                            或者对于有很多地方调用的，调用起来还要封装一个对象，会不太方便
     * @return
     */
    public ResponseResult<TokenResponse> checkCode(VerificationCodeDTO verificationCodeDTO) {
        // 去redis 读取验证码
        String key = RedisPrefixUtils.getPhoneKey(verificationCodeDTO.getDriverPhone(), IdentityConstants.DRIVER_IDENTITY);
        String codeRedis = stringRedisTemplate.opsForValue().get(key);
        // 校验验证码
        if (StringUtils.isBlank(codeRedis)) {
            return ResponseResult.fail(CommonStatusEnum.VERIFICATION_CODE_ERR);
        }
        if (!StringUtils.equals(codeRedis, verificationCodeDTO.getVerificationCode())) {
            return ResponseResult.fail(CommonStatusEnum.VERIFICATION_CODE_ERR);
        }

        // 颁发令牌
        String accessToken = JwtUtils.generatorToken(verificationCodeDTO.getDriverPhone(), IdentityConstants.DRIVER_IDENTITY, TokenConstants.ACCESS_TOKEN_TYPE);
        String refreshToken = JwtUtils.generatorToken(verificationCodeDTO.getDriverPhone(), IdentityConstants.DRIVER_IDENTITY, TokenConstants.REFRESH_TOKEN_TYPE);
        // 将token存到redis当中
        String accessTokenKey = RedisPrefixUtils.generatorTokenKey(verificationCodeDTO.getDriverPhone(), IdentityConstants.DRIVER_IDENTITY, TokenConstants.ACCESS_TOKEN_TYPE);
        String refreshTokenKey = RedisPrefixUtils.generatorTokenKey(verificationCodeDTO.getDriverPhone(), IdentityConstants.DRIVER_IDENTITY, TokenConstants.REFRESH_TOKEN_TYPE);
        stringRedisTemplate.opsForValue().set(accessTokenKey, accessToken, 30, TimeUnit.DAYS);
        stringRedisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 31, TimeUnit.DAYS);

        // 响应
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        return ResponseResult.success(tokenResponse);
    }


}
