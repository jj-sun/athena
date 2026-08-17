/*
package com.athena.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

*/
/**
 * jwt工具类
 *
 * @author Mr.sun
 *//*

public class JwtUtils {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    */
/**
     * token过期时间为30分钟，以token在redis缓存的时间为准。
     *//*

    public static final long EXPIRE = 30 * 60 * 1000;

    */
/**
     * 生成jwt token
     *//*

    public static String generateToken(String username, String secret) {
        Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + EXPIRE);

        return JWT.create()
                .withIssuer("ATHENA")
                .withIssuedAt(nowDate)
                .withExpiresAt(expireDate)
                .withSubject(username)
                .sign(Algorithm.HMAC256(secret));
    }

    public static DecodedJWT getDecodedJWTByToken(String token) {
        return JWT.decode(token);
    }

    public static String getUsernameByToken(String token) {
        return getDecodedJWTByToken(token).getSubject();
    }

    public static boolean verify(String token, String username, String secret) {
        try{

            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).withSubject(username).withIssuer("ATHENA").build();
            return Objects.nonNull(verifier.verify(token));
        }catch (Exception e) {
            return false;
        }
    }

    */
/**
     * token是否过期
     * @return  true：过期
     *//*

    public static boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

}
*/
