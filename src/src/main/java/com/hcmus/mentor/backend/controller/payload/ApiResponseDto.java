package com.hcmus.mentor.backend.controller.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer returnCode;

    public ApiResponseDto(boolean success, String message, Integer returnCode) {
        this.success = success;
        this.message = message;
        this.returnCode = returnCode;
    }

    public ApiResponseDto(T data, Integer returnCode) {
        this(true, null, returnCode);
        this.data = data;
    }

    public ApiResponseDto(T data, Integer returnCode, String message) {
        this(true, message, returnCode);
        this.data = data;
    }

    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(data, ReturnCodeConstants.SUCCESS);
    }

    public static <T> ApiResponseDto<T> failure(String message, Integer returnCode) {
        return new ApiResponseDto<>(null, returnCode, message);
    }

    public static <T> ApiResponseDto<T> notFound(Integer returnCode) {
        return new ApiResponseDto<>(false, "Not found", returnCode);
    }
}
