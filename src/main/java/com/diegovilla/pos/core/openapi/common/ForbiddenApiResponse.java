package com.diegovilla.pos.core.openapi.common;

import com.diegovilla.pos.core.errors.dtos.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.*;

/**
 * Meta-annotation that defines a reusable OpenAPI {@code 403 Forbidden} response.
 *
 * <p>Apply this annotation to endpoint-level documentation annotations to automatically include a
 * standardized forbidden response with an example {@link ErrorResponseDTO} payload.
 *
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "403",
        description = "You do not have permission to perform this operation.",
        content =
                @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponseDTO.class),
                        examples =
                                @ExampleObject(
                                        name = "ForbiddenResponse",
                                        value =
                                                """
        {
          "timestamp": "2026-10-28T17:15:00Z",
          "status": 403,
          "error": "Forbidden",
          "message": "The resource belongs to another cashier.",
          "errors": null
        }
        """)))
public @interface ForbiddenApiResponse {}
