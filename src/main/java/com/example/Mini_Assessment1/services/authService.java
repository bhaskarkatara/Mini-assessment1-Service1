package com.example.Mini_Assessment1.services;
import com.example.Mini_Assessment1.dataClass.ApiResponse;
import com.example.Mini_Assessment1.dataClass.OtpDetails;
import com.example.Mini_Assessment1.dataClass.User;
import com.example.Mini_Assessment1.dataClass.loginDto;
import com.example.Mini_Assessment1.jwtUtils.jwtUtils;
import com.example.Mini_Assessment1.repository.authRepo;
import com.example.Mini_Assessment1.passwordUtils.passwordUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class authService {

    @Autowired
    private authRepo AuthRepo;

    @Autowired
    private passwordUtils PasswordUtils;

    @Autowired
    private jwtUtils JwtUtils;

    @Autowired
    private emailService EmailService;

    Map<String, OtpDetails> otpStore = new HashMap<>();

    public Optional<User> findByEmail(String email) {
        return AuthRepo.findByEmail(email);
    }

    public ResponseEntity<?> signupToOtp(User signupRequest) {
        try {
            String userEmail = signupRequest.getEmail();
            if (AuthRepo.existsByEmail(signupRequest.getEmail())) {
                throw new RuntimeException("user already exits");
            }
            return generateOtp(userEmail);
        } catch (Exception e) {
             return ResponseEntity.ok(new ApiResponse<>(500,"Something went wrong in backend code",e));
        }

    }

    public ResponseEntity<?> generateOtp(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP

        // Save OTP with email
        otpStore.put(email, new OtpDetails(otp, System.currentTimeMillis()));

        // Send OTP via email
        EmailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);

     return ResponseEntity.ok(new ApiResponse<>(200,"Otp Sent Successfully",null));
    }

    public void save(User user) {
        AuthRepo.save(user);
    }


    public ResponseEntity<?> signup(User signupRequest) {
        String hashPassword = PasswordUtils.hashPassword(signupRequest.getPassword());
        User user = new User(new ObjectId(), signupRequest.getName(), signupRequest.getEmail(), hashPassword);
        AuthRepo.save(user);
        return ResponseEntity.ok(new ApiResponse<>(200,"Signup Successfully",null));
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

        ResponseEntity<?> signupResponse = signup(signupRequest);

        String otpResponse = "OTP Verified and ";
        String combinedMessage = otpResponse + signupResponse.getBody();
        return ResponseEntity.ok(new ApiResponse<>(201, combinedMessage, null));
    }


    public ResponseEntity<?> login(loginDto loginDto) {

        try {
            User user = AuthRepo.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("user not found"));

            if (!PasswordUtils.matchPassword(loginDto.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid Credentials");
            }
            String jwt = JwtUtils.generateToken(loginDto.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(200, "Login Successfully", jwt));

        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse<>(401, "Login Failed", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(500, "Something went Wrong", e));
        }
    }
}