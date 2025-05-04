package com.example.Mini_Assessment1.services;
import com.example.Mini_Assessment1.dataClass.ApiResponse;
import com.example.Mini_Assessment1.dataClass.OtpDetails;
import com.example.Mini_Assessment1.dataClass.User;
import com.example.Mini_Assessment1.dataClass.LoginDto;
import com.example.Mini_Assessment1.jwtutils.JwtUtils;
import com.example.Mini_Assessment1.passwordutils.PasswordUtils;
import com.example.Mini_Assessment1.repository.AuthRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private AuthRepo authRepo;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    Map<String, OtpDetails> otpStore = new HashMap<>();

    public Optional<User> findByEmail(String email) {
        return authRepo.findByEmailIgnoreCase(email);
    }

    public ResponseEntity<?> signupToOtp(User signupRequest) {
        try {
            String userEmail = signupRequest.getEmail();
            if (authRepo.existsByEmailIgnoreCase(signupRequest.getEmail())) {
                throw new RuntimeException("user already exits");
            }
            return generateOtp(userEmail);
        } catch (Exception e) {
             return ResponseEntity.ok(new ApiResponse<>(401,e.getMessage(),null));
        }

    }

    public ResponseEntity<?> generateOtp(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP

        // Save OTP with email
        otpStore.put(email, new OtpDetails(otp, System.currentTimeMillis()));

        // Send OTP via email
        emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);

     return ResponseEntity.ok(new ApiResponse<>(200,"Otp Sent Successfully",null));
    }

    public void save(User user) {
        authRepo.save(user);
    }


    public ResponseEntity<?> signup(User signupRequest) {
        String hashPassword = passwordUtils.hashPassword(signupRequest.getPassword());
        User user = new User(new ObjectId(), signupRequest.getName(), signupRequest.getEmail(), hashPassword);
        authRepo.save(user);

        // generate Jwt here
        String jwt = jwtUtils.generateToken(signupRequest.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(200,"Signup Successfully",jwt));
    }


    public ResponseEntity<?> verifyOtp(int enteredOtp, User signupRequest) {
        String email = signupRequest.getEmail();
        OtpDetails otpDetails = otpStore.get(email);
        if (otpDetails == null) {
            return ResponseEntity.ok(new ApiResponse<>(400,("OTP Not Found"),null));
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - otpDetails.getTimestamp() > 5 * 60 * 1000) { // 5 min expiry
            return ResponseEntity.ok(new ApiResponse<>(400,"OTP  expired",null));
        }

        if (otpDetails.getOtp() != enteredOtp) {
            return ResponseEntity.ok(new ApiResponse<>(400,"Invalid OTP",null));
        }
        otpStore.clear();
        // OTP verified – now save user in DB
        if(!checkForEmptyField(signupRequest)){
            return ResponseEntity.ok(new ApiResponse<>(400,"please enter all details",null));
        }
        return signup(signupRequest);
    }

    public Boolean checkForEmptyField(User signUpRequest){
        return !signUpRequest.getEmail().isEmpty() &&
                !signUpRequest.getPassword().isEmpty() &&
                !signUpRequest.getName().isEmpty();
    }

    public ResponseEntity<?> login(LoginDto loginDto) {

        try {
            User user = authRepo.findByEmailIgnoreCase(loginDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("user not found"));

            if (!passwordUtils.matchPassword(loginDto.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid Credentials");
            }
            return ResponseEntity.ok(new ApiResponse<>(200, "Login Successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse<>(401, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(500, "Something went Wrong", e));
        }
    }
}