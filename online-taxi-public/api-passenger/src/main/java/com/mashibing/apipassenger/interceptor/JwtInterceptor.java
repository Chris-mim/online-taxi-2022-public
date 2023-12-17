package com.mashibing.apipassenger.interceptor;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.mashibing.internalcommon.constant.ResponseResult;
import com.mashibing.internalcommon.dto.TokenResult;
import com.mashibing.internalcommon.util.JwtUtils;
import com.mashibing.internalcommon.util.RedisPrefixUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean result = true;
        String resultString = "";
        TokenResult tokenResult = null;
        String token = request.getHeader("Authorization");
        try {
            tokenResult = JwtUtils.parseToken(token);
        }catch (SignatureVerificationException e){
            resultString = "token sign error";
            result = false;
        }catch (TokenExpiredException e){
            resultString = "token time out";
            result = false;
        }catch (AlgorithmMismatchException e){
            resultString = "token AlgorithmMismatchException";
            result = false;
        }catch (Exception e){
            resultString = "token invalid";
            result = false;
        }
        // 获取Redis里存的token 与传入token做比较
        if (tokenResult == null){
            resultString = "token invalid";
            result = false;
        }else{
            // 获取token key
            String tokenRedisKey = RedisPrefixUtils.generatorTokenKey(tokenResult.getPhone(),tokenResult.getIdentity());
            String tokenRedis = stringRedisTemplate.opsForValue().get(tokenRedisKey);
            // 比较传入的Token和Redis里的Token
            if (!StringUtils.equals(tokenRedis,token)){
                resultString = "token invalid";
                result = false;
            }
        }


        if(!result){
            PrintWriter out = response.getWriter();
            out.print(JSONObject.fromObject(ResponseResult.fail(resultString)).toString());
        }


        return result;
    }
}
