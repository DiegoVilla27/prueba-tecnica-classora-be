package com.diegovilla.pos.features.sale.infrastructure.docs;

import com.diegovilla.pos.core.openapi.common.BadRequestApiResponse;
import com.diegovilla.pos.core.openapi.common.ConflictApiResponse;
import com.diegovilla.pos.core.openapi.common.ForbiddenApiResponse;
import com.diegovilla.pos.core.openapi.common.InternalServerErrorApiResponse;
import com.diegovilla.pos.core.openapi.common.NotFoundApiResponse;
import com.diegovilla.pos.core.openapi.common.TooManyRequestsApiResponse;
import com.diegovilla.pos.core.openapi.common.UnauthorizedApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aggregates the OpenAPI metadata for the <em>Cancel sale</em> endpoint.
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Cancel sale",
        description = "Cancels (empties) an open sale. The checkout then opens a new one.")
@ApiResponse(responseCode = "204", description = "Sale cancelled.")
@BadRequestApiResponse
@UnauthorizedApiResponse
@ForbiddenApiResponse
@NotFoundApiResponse
@ConflictApiResponse
@TooManyRequestsApiResponse
@InternalServerErrorApiResponse
public @interface CancelSaleDocumentation {}
