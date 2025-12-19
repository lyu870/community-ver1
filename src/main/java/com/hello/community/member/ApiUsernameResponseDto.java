// ApiUsernameResponseDto.java
package com.hello.community.member;

public class ApiUsernameResponseDto {

    private boolean success;
    private String username;
    private String message;
    private String error;

    public ApiUsernameResponseDto(boolean success, String username, String message, String error) {
        this.success = success;
        this.username = username;
        this.message = message;
        this.error = error;
    }

    public static ApiUsernameResponseDto ok(String username) {
        return new ApiUsernameResponseDto(true, username, null, null);
    }

    public static ApiUsernameResponseDto fail(String error) {
        return new ApiUsernameResponseDto(false, null, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
