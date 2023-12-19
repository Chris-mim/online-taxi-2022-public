package com.mashibing.apipassenger.controller;

import com.mashibing.apipassenger.service.TokenService;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @Autowired
    private TokenService tokenService;

    /**
     * 根据refreshToken生成accessToken接口
     * @param tokenResponse
     * @return
     */
    @PostMapping("/refresh-token")
    public ResponseResult refreshToken(@RequestBody TokenResponse tokenResponse){
        return tokenService.refreshToken(tokenResponse.getRefreshToken());

    }
}
