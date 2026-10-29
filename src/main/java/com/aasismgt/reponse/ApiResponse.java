package com.aasismgt.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean status;
    private String message;
    private Object data;

    public ApiResponse success(String message, Object data) {
        this.status = true;
        this.message = message;
        this.data = data;
        return this;
    }

    public ApiResponse error(String message) {
        this.status = false;
        this.message = message;
        return this;
    }
}
