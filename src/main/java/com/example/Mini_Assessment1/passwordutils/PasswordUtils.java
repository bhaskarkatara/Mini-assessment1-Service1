package com.example.Mini_Assessment1.passwordutils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
 private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

 public String hashPassword(String PlainPassword){
     return encoder.encode(PlainPassword);
 }

 public Boolean matchPassword(String rawPassword, String hashedPassword){
     return encoder.matches(rawPassword,hashedPassword);
 }
}
