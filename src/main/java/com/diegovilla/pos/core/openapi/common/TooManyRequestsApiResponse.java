package com.diegovilla.pos.core.openapi.common;

import com.diegovilla.pos.core.errors.dtos.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.*;

/**
 * Meta-annotation that defines a reusable OpenAPI {@code 429 Too Many Requests} response.
 *
 * <p>Apply this annotation to endpoint-level documentation annotations to automatically include a
 * standardized too many requests response with an example {@link ErrorResponseDTO} payload.
 *
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "429",
        description = "The client has exceeded the allowed request rate.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponseDTO.class),
                        examples =
                                @ExampleObject(
                                        name = "TooManyRequestsResponse",
                                        value =
                                                """
        {
          "timestamp": "2026-10-28T17:15:00Z",
          "status": 429,
          "error": "Too Many Requests",
          "message": "You have exceeded your request limit. Try again in 42 seconds.",
          "errors": null
        }
        """)))
public @interface TooManyRequestsApiResponse {}
