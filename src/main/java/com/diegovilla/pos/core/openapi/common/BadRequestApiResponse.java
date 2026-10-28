package com.diegovilla.pos.core.openapi.common;

import com.diegovilla.pos.core.errors.dtos.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.*;

/**
 * Meta-annotation that defines a reusable OpenAPI {@code 400 Bad Request} response.
 *
 * <p>Apply this annotation to endpoint-level documentation annotations to automatically include a
 * standardized bad-request response with an example {@link ErrorResponseDTO} payload containing
 * field-level validation errors.
 *
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "400",
        description = "The request is invalid or contains validation errors.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponseDTO.class),
                        examples =
                                @ExampleObject(
                                        name = "BadRequestResponse",
                                        value =
                                                """
        {
          "timestamp": "2026-10-28T17:15:00Z",
          "status": 400,
          "error": "Bad Request",
          "message": "The request contains validation errors.",
          "errors": [
            {
              "field": "title",
              "value": "ab",
              "message": "Code must contain 4 to 14 digits"
            }
          ]
        }
        """)))
public @interface BadRequestApiResponse {}
