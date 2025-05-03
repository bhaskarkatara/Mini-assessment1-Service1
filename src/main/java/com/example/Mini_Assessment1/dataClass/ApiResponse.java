package com.example.Mini_Assessment1.dataClass;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;

    public ApiResponse(int status, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
