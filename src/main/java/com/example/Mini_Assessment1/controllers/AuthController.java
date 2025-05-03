package com.example.Mini_Assessment1.controllers;

import com.example.Mini_Assessment1.dataClass.ApiResponse;
import com.example.Mini_Assessment1.dataClass.OtpRequest;
import com.example.Mini_Assessment1.dataClass.User;
import com.example.Mini_Assessment1.dataClass.LoginDto;
import com.example.Mini_Assessment1.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // call this Api when user click for Otp to verify email
    @PostMapping("/signup-to-otp")
     public ResponseEntity<?> signupToOtp(@RequestBody User signUpRequest){
        return authService.signupToOtp(signUpRequest);
    }


  // here comes otp
    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest){
        User signupRequest = otpRequest.getUser(); // null check
        if (signupRequest == null) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User data is missing");
        }
       return authService.verifyOtp(otpRequest.getOtp(), otpRequest.getUser());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginRequest){
        if(loginRequest.getPassword().isEmpty() || loginRequest.getEmail().isEmpty()){
            return ResponseEntity.ok(new ApiResponse<>(400,"please enter your credentials",null));
        }
        return authService.login(loginRequest);
    }

}
