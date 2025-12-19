// ApiMessageResponseDto.java
package com.hello.community.member;

public class ApiMessageResponseDto {

    private boolean success;
    private String message;
    private String error;

    public ApiMessageResponseDto(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    public static ApiMessageResponseDto ok(String message) {
        return new ApiMessageResponseDto(true, message, null);
    }

    public static ApiMessageResponseDto ok() {
        return new ApiMessageResponseDto(true, null, null);
    }

    public static ApiMessageResponseDto fail(String error) {
        return new ApiMessageResponseDto(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
