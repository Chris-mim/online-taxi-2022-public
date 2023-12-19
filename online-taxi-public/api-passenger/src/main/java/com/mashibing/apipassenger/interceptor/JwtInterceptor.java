package com.mashibing.apipassenger.interceptor;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.constant.TokenConstants;
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
        String token = request.getHeader("Authorization");
        TokenResult tokenResult = JwtUtils.checkToken(token);

        // 获取Redis里存的token 与传入token做比较
        if (tokenResult == null){
            resultString = "token invalid";
            result = false;
        }else{
            // 获取token key
            String tokenRedisKey = RedisPrefixUtils.generatorTokenKey(tokenResult.getPhone(),tokenResult.getIdentity(), TokenConstants.ACCESS_TOKEN_TYPE);
            String tokenRedis = stringRedisTemplate.opsForValue().get(tokenRedisKey);
            // 比较传入的Token和Redis里的Token
            if (StringUtils.isBlank(token) || !StringUtils.equals(tokenRedis,token)){
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
