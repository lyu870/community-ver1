// ApiResponseDto.java
package com.hello.community.board.common;

public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private String errorCode;
    private String error;
    private T data;

    public ApiResponseDto(boolean success, String message, String error, T data) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.data = data;
        this.errorCode = null;
    }

    public ApiResponseDto(boolean success, String message, String errorCode, String error, T data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.error = error;
        this.data = data;
    }

    public static <T> ApiResponseDto<T> ok() {
        return new ApiResponseDto<>(true, null, null, null);
    }

    public static <T> ApiResponseDto<T> ok(String message) {
        return new ApiResponseDto<>(true, message, null, null);
    }

    public static <T> ApiResponseDto<T> ok(T data) {
        return new ApiResponseDto<>(true, null, null, data);
    }

    public static <T> ApiResponseDto<T> ok(String message, T data) {
        return new ApiResponseDto<>(true, message, null, data);
    }

    public static <T> ApiResponseDto<T> fail(String error) {
        return new ApiResponseDto<>(false, null, null, error, null);
    }

    public static <T> ApiResponseDto<T> fail(String errorCode, String error) {
        return new ApiResponseDto<>(false, null, errorCode, error, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public T getData() {
        return data;
    }
}
