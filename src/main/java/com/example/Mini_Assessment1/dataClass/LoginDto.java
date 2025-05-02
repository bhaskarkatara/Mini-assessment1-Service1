package com.example.Mini_Assessment1.dataClass;

public class LoginDto {
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    private String email;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;
}
