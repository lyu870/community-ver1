// ApiProfileResponseDto.java
package com.hello.community.member;

public class ApiProfileResponseDto {

    private boolean success;
    private String message;
    private String newDisplayName;
    private String error;

    public ApiProfileResponseDto(boolean success, String message, String newDisplayName, String error) {
        this.success = success;
        this.message = message;
        this.newDisplayName = newDisplayName;
        this.error = error;
    }

    public static ApiProfileResponseDto ok(String message, String newDisplayName) {
        return new ApiProfileResponseDto(true, message, newDisplayName, null);
    }

    public static ApiProfileResponseDto fail(String error) {
        return new ApiProfileResponseDto(false, null, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getNewDisplayName() {
        return newDisplayName;
    }

    public String getError() {
        return error;
    }
}
