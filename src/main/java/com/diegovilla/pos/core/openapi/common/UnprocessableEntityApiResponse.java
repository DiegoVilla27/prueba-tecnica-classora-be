package com.diegovilla.pos.core.openapi.common;

import com.diegovilla.pos.core.errors.dtos.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.*;

/**
 * Meta-annotation that defines a reusable OpenAPI {@code 422 Unprocessable Content} response.
 *
 * <p>Apply this annotation to endpoint-level documentation annotations to automatically include a
 * standardized unprocessable content response with an example {@link ErrorResponseDTO} payload.
 *
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "422",
        description = "The request is valid but violates a business rule.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponseDTO.class),
                        examples =
                                @ExampleObject(
                                        name = "UnprocessableEntityResponse",
                                        value =
                                                """
        {
          "timestamp": "2026-10-28T17:15:00Z",
          "status": 422,
          "error": "Unprocessable Content",
          "message": "A sale without lines cannot be completed.",
          "errors": null
        }
        """)))
public @interface UnprocessableEntityApiResponse {}
