package com.mashibing.internalcommon.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    // 盐
    private static final String SIGN = "CPFmsb!@#$$";

    private static final String JWT_KEY = "passengerPhone";

    public static String generatorToken(String passengerPhone){
        Map<String,String> map = new HashMap<>();
        map.put(JWT_KEY, passengerPhone);

        // token过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date date = calendar.getTime();

        JWTCreator.Builder builder = JWT.create();
        // 整合map
        map.forEach((k,v) -> {
            builder.withClaim(k,v);
        });
        // 整合过期时间
        builder.withExpiresAt(date);
        // 生成token
        return builder.sign(Algorithm.HMAC256(SIGN));
    }

    // 解析token
    public static String parseToken(String token){
        DecodedJWT verify = JWT.require(Algorithm.HMAC256(SIGN)).build().verify(token);
        Claim claim = verify.getClaim(JWT_KEY);
        return claim.toString();

    }

    public static void main(String[] args) {

        String s = generatorToken("13910733522");
        System.out.println("生成的token："+s);

        // 当只修改JWT里的数据信息，而不同步修改签名时，直接报错：签名无效
//        System.out.println("解析token后的值："+parseToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXNzZW5nZXJQaG9uZSI6IjEzOTEwNzMzNTIyIiwiZXhwIjoxNzAyODg5Nzc5fQ.Ut6uPJt3peKSVbPM1ONtzby3XCWVTLHvr6Di7lxSf6w"));
        // 正常值
        System.out.println("解析token后的值："+parseToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXNzZW5nZXJQaG9uZSI6IjEzOTEwNzMzNTIyIiwiZXhwIjoxNzAyODg5ODA1fQ.e4W-ZFl38pi997I1Od8GqO1p-snu_0PFl2PKC2h3Keg"));
    }
}
