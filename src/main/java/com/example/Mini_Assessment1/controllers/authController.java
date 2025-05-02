package com.example.Mini_Assessment1.controllers;

import com.example.Mini_Assessment1.dataClass.OtpRequest;
import com.example.Mini_Assessment1.dataClass.User;
import com.example.Mini_Assessment1.dataClass.loginDto;
import com.example.Mini_Assessment1.jwtUtils.jwtUtils;
import com.example.Mini_Assessment1.services.authService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class authController {


    @Autowired
    private jwtUtils JwtUtils;
    @Autowired
    private authService AuthService;

    // call this Api when user click for Otp to verify his/her email
    @PostMapping("/signup-to-otp")
     public ResponseEntity<?> signupToOtp(@RequestBody User signUpRequest){
        return AuthService.signupToOtp(signUpRequest);
    }


  // here comes otp
    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest){
        User signupRequest = otpRequest.getUser(); // null check zaruri
        if (signupRequest == null) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User data is missing");
        }
       return AuthService.verifyOtp(otpRequest.getOtp(), otpRequest.getUser());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody loginDto loginRequest){
        return AuthService.login(loginRequest);
    }

}
