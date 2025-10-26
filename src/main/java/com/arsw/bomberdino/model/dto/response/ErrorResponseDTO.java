package com.arsw.bomberdino.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Standardized error response structure for REST API and WebSocket errors.
 * Used by GlobalExceptionHandler for consistent error communication.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    
    /**
     * HTTP status code or WebSocket error code
     */
    @NotNull(message = "Status code cannot be null")
    private Integer statusCode;
    
    /**
     * Human-readable error message for client display
     */
    @NotBlank(message = "Error message cannot be blank")
    private String message;
    
    /**
     * Machine-readable error code for client-side error handling.
     * Format: DOMAIN_ERROR_TYPE (e.g., GAME_SESSION_FULL, PLAYER_ALREADY_DEAD)
     */
    @NotBlank(message = "Error code cannot be blank")
    private String errorCode;
    
    /**
     * Timestamp when error occurred (epoch milliseconds)
     */
    @NotNull(message = "Timestamp cannot be null")
    private Long timeStamp;
    
    /**
     * Creates an ErrorResponseDTO with current timestamp
     * 
     * @param statusCode HTTP or custom error status code
     * @param message human-readable error description
     * @param errorCode machine-readable error identifier
     * @return populated ErrorResponseDTO instance
     */
    public static ErrorResponseDTO of(Integer statusCode, String message, String errorCode) {
        return ErrorResponseDTO.builder()
                .statusCode(statusCode)
                .message(message)
                .errorCode(errorCode)
                .timeStamp(System.currentTimeMillis())
                .build();
    }
}
