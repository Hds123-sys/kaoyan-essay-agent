package com.essay.agent.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;

    private String message;

    private T data;

    private Map<String, Object> meta;

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(0, "success", null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, null);
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(0, "success", data, meta);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }

}