package com.example.Mini_Assessment1.controllers;

import com.example.Mini_Assessment1.dataClass.ApiResponse;
import com.example.Mini_Assessment1.dataClass.User;
import com.example.Mini_Assessment1.jwtUtils.jwtUtils;


import com.example.Mini_Assessment1.services.authService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
public class GoogleOAuthController {

    @Autowired
    private jwtUtils jwtUtils;

    @Autowired
    private authService AuthService;

    @Autowired
    private RestTemplate restTemplate;

    private static final BCryptPasswordEncoder PasswordEncoder = new BCryptPasswordEncoder();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> HandleOauthCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error
    ) {
try{

    if (error != null) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:3000/login?error=" + error));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    if (code == null) {
        return ResponseEntity.badRequest().body("Missing authorization code.");
    }
    //  Exchange code for access token
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", code);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", redirectUri);
    params.add("grant_type", "authorization_code");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://oauth2.googleapis.com/token", request, Map.class);

    String accessToken = Objects.requireNonNull(response.getBody()).get("access_token").toString();

    //  Fetch user info
    HttpHeaders userHeaders = new HttpHeaders();
    userHeaders.setBearerAuth(accessToken);
    HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

    ResponseEntity<Map> userResponse = restTemplate.exchange(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            HttpMethod.GET,
            userRequest,
            Map.class
    );

    Map userInfo = userResponse.getBody();

    assert userInfo != null;
    String email = userInfo.get("email") != null ? userInfo.get("email").toString() : "no-email@example.com";
    String name = userInfo.get("name") != null ? userInfo.get("name").toString() : "No Name";


    //  Check user in DB
    Optional<User> existingUser = AuthService.findByEmail(email);
    if (existingUser.isEmpty()) {
        AuthService.save(new User(name,email,PasswordEncoder.encode(UUID.randomUUID().toString())));
    }

    //  Generate JWT
    String jwt = jwtUtils.generateToken(email);

    //todo : kya ye token bejna jaruri hai
    String frontendRedirectUrl = "http://localhost:3000/dashboard?token=" + jwt;
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setLocation(URI.create(frontendRedirectUrl));
    return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);

}catch (Exception e){
     e.printStackTrace();
//     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    return ResponseEntity.ok(new ApiResponse<>(500,"Something went wrong",null));
}
    }
}
