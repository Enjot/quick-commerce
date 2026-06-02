package com.enjot.quickcommerce.web.dto;

import java.util.List;

/**
 * Uniform error payload returned by the global exception handler.
 */
public record ApiError(
        int status,
        String error,
        String message,
        List<String> details) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, List.of());
    }

    public static ApiError of(int status, String error, String message, List<String> details) {
        return new ApiError(status, error, message, details);
    }
}
