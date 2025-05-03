package com.example.Mini_Assessment1.jwtutils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${spring.secret.key}")
    private String SECRET_KEY;

    private SecretKey getSignkey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    public String generateToken(String email){
        Map<String, Object>claims = new HashMap<>();
        return createToken(claims,email);
    }

    private String createToken(Map<String,Object>claims,String subject){
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60  * 60))
                .signWith(getSignkey())
                .compact();
    }
}
