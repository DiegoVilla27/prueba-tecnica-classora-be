package com.diegovilla.pos.features.product.infrastructure.controllers;

import com.diegovilla.pos.features.product.application.services.ProductService;
import com.diegovilla.pos.features.product.infrastructure.docs.GetProductByCodeDocumentation;
import com.diegovilla.pos.features.product.infrastructure.dto.response.ProductResponseDTO;
import com.diegovilla.pos.features.product.infrastructure.mappers.ProductDtoMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the product catalog lookup used by the checkout.
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Looks up catalog products by their scannable code.")
public class ProductController {

    private final ProductService productService;
    private final ProductDtoMapper productDtoMapper;

    /**
     * Retrieves a product by its scannable code.
     *
     * @param code product code (4–14 digits).
     * @return HTTP 200 with the {@link ProductResponseDTO}.
     */
    @GetMapping("/{code}")
    @GetProductByCodeDocumentation
    public ResponseEntity<ProductResponseDTO> getByCode(
            @Parameter(description = "Scannable product code", example = "8410001000011")
                    @PathVariable
                    @Pattern(regexp = "^\\d{4,14}$", message = "Code must contain 4 to 14 digits")
                    String code) {
        return ResponseEntity.ok(
                productDtoMapper.modelToResponseDTO(productService.getByCode(code)));
    }
}
