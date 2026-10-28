package com.diegovilla.pos.features.product.infrastructure.mappers;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.product.infrastructure.dto.response.ProductResponseDTO;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting product domain models into HTTP response DTOs.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface ProductDtoMapper {

    /**
     * Converts a domain model into its response DTO.
     *
     * @param productModel product to convert.
     * @return a {@link ProductResponseDTO} without audit timestamps.
     */
    ProductResponseDTO modelToResponseDTO(ProductModel productModel);
}
