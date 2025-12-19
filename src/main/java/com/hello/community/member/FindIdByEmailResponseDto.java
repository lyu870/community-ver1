// FindIdByEmailResponseDto.java
package com.hello.community.member;

public class FindIdByEmailResponseDto {

    private String status;
    private String username;
    private String message;

    public FindIdByEmailResponseDto(String status, String username, String message) {
        this.status = status;
        this.username = username;
        this.message = message;
    }

    public static FindIdByEmailResponseDto ok(String username) {
        return new FindIdByEmailResponseDto("OK", username, null);
    }

    public static FindIdByEmailResponseDto fail(String message) {
        return new FindIdByEmailResponseDto("ERROR", null, message);
    }

    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
