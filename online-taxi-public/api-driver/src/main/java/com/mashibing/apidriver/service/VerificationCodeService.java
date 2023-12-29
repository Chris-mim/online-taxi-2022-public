package com.mashibing.apidriver.service;

import com.mashibing.apidriver.remote.ServiceDriverUserClient;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.DriverCarConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.DriverUserExistsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationCodeService {


    @Autowired
    private ServiceDriverUserClient serviceDriverUserClient;

    /**
     * 生成验证码
     *
     * @param driverPhone 手机号
     * @return
     */
    public ResponseResult generatorCode(String driverPhone) {
        // 1、验证码司机手机号是否存在
        log.info("手机号："+driverPhone);
        ResponseResult<DriverUserExistsResponse> driverUserExistsResponse = serviceDriverUserClient.getUser(driverPhone);

        DriverUserExistsResponse driverUser = driverUserExistsResponse.getData();
        if (driverUser.getIfExists() == DriverCarConstants.DRIVER_NOT_EXISTS){
            return ResponseResult.fail(CommonStatusEnum.DRIVER_NOT_EXISTS);
        }
        log.info("司机存在:" + driverPhone);
        // 2、调用验证码服务，获取验证码
//        ResponseResult<NumberCodeResponse> numberCodeResponse = serviceVerificationcodeClient.getNumberCode(6);


        // 3、存入Redis
        // key,value,过期时间

        // 4、第三方调用，通过短信服务商，将对应的验证码发送到手机上。阿里短信服务，腾讯短信通，华信，容联


        return ResponseResult.success();

    }




}
