package com.example.Mini_Assessment1.controllers;

import com.example.Mini_Assessment1.dataClass.ApiResponse;
import com.example.Mini_Assessment1.dataClass.User;
import com.example.Mini_Assessment1.jwtutils.JwtUtils;
import com.example.Mini_Assessment1.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
public class FbOAuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    private static final BCryptPasswordEncoder PasswordEncoder = new BCryptPasswordEncoder();

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String redirectUri;

    @GetMapping("/oauth2/facebook/callback")
    public ResponseEntity<?> facebookCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error
    ) {
        if (error != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("http://localhost:3000/login?error=" + error));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        if (code == null) {
            return ResponseEntity.badRequest().body("Missing code parameter");
        }
        try{
            // Exchange code for access token
            String tokenUrl = "https://graph.facebook.com/v15.0/oauth/access_token" +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&client_secret=" + clientSecret +
                    "&code=" + code;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);
            String accessToken = (String) Objects.requireNonNull(response.getBody()).get("access_token");

            // Fetch user profile
            String userInfoUrl = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            Map userInfo = userInfoResponse.getBody();


            assert userInfo != null;
            String email = userInfo.get("email") != null ? userInfo.get("email").toString() : "no-email@example.com";
            String name = userInfo.get("name") != null ? userInfo.get("name").toString() : "No Name";

           // Check user in DB
            Optional<User> existingUser = authService.findByEmail(email);
            if (existingUser.isEmpty()) {
                authService.save(new User(name,email,PasswordEncoder.encode(UUID.randomUUID().toString())));
            }

            //  Generate JWT
            String jwt = jwtUtils.generateToken(email);

            //  Redirect back to frontend with token or user info
            String redirectFrontend = "http://localhost:3000/dashboard?token=" + jwt;
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectFrontend));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(500,"Something went wrong",null));
        }

    }
}
