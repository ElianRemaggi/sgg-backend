package com.sgg.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wrapper estándar para todas las respuestas de la API.
 *
 * Formato exitoso:  { "success": true,  "data": {...}, "error": null }
 * Formato error:    { "success": false, "data": null,  "error": {"code": 404, "message": "..."} }
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message));
    }
}
