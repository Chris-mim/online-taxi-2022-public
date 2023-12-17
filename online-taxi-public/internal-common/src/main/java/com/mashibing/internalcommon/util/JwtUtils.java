package com.mashibing.internalcommon.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mashibing.internalcommon.dto.TokenResult;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    // 盐
    private static final String SIGN = "CPFmsb!@#$$";

    /**
     * 司机和乘客用的可能是同一个手机号，无法区分，所以加一个身份标识identity
     */
    private static final String JWT_KEY_PHONE = "phone";

    // 乘客是1，司机是2
    private static final String JWT_KEY_IDENTITY = "identity";


    public static String generatorToken(String phone, String identity){
        Map<String,String> map = new HashMap<>();
        map.put(JWT_KEY_PHONE, phone);
        map.put(JWT_KEY_IDENTITY, identity);

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
    public static TokenResult parseToken(String token){
        DecodedJWT verify = JWT.require(Algorithm.HMAC256(SIGN)).build().verify(token);
        Claim claim = verify.getClaim(JWT_KEY_PHONE);
        String phone = verify.getClaim(JWT_KEY_PHONE).asString(); // asString带一个双引号，toString会带两个双引号
        String identity = verify.getClaim(JWT_KEY_IDENTITY).asString();

        TokenResult tokenResult = new TokenResult();
        tokenResult.setPhone(phone);
        tokenResult.setIdentity(identity);
        return tokenResult;

    }

    public static void main(String[] args) {

        String s = generatorToken("13910733521" , "1");
        System.out.println("生成的token："+s);
        System.out.println("解析-----------------");
        TokenResult tokenResult = parseToken(s);
        System.out.println("手机号："+tokenResult.getPhone());
        System.out.println("身份："+tokenResult.getIdentity());

        // 当只修改JWT里的数据信息，而不同步修改签名时，直接报错：签名无效
    }
}
