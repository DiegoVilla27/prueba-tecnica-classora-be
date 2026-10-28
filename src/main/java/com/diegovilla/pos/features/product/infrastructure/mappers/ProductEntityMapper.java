package com.diegovilla.pos.features.product.infrastructure.mappers;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.product.infrastructure.entity.ProductEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting {@link ProductEntity} into {@link ProductModel}.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface ProductEntityMapper {

    /**
     * Converts a persistence entity into a domain model.
     *
     * @param productEntity persistence entity to convert.
     * @return a reconstructed {@link ProductModel}, or {@code null} if input is null.
     */
    default ProductModel entityToModel(ProductEntity productEntity) {
        if (productEntity == null) {
            return null;
        }
        return ProductModel.reconstruct(
                productEntity.getId(),
                productEntity.getCode(),
                productEntity.getName(),
                productEntity.getPrice(),
                productEntity.getCreatedAt(),
                productEntity.getUpdatedAt());
    }
}
