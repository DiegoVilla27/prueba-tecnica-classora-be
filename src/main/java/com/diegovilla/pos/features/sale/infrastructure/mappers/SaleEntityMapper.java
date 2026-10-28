package com.diegovilla.pos.features.sale.infrastructure.mappers;

import com.diegovilla.pos.features.sale.domain.model.SaleLine;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.infrastructure.entity.SaleEntity;
import com.diegovilla.pos.features.sale.infrastructure.entity.SaleLineEntity;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * Mapper converting between the sale aggregate and its JPA entities.
 *
 * <p>Written by hand (MapStruct {@code default} methods) because the aggregate is rebuilt through
 * {@code reconstruct} and the line order is stored as {@code position}.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SaleEntityMapper {

    /**
     * Converts a persistence entity, with its lines, into the aggregate.
     *
     * @param saleEntity persistence entity to convert.
     * @return a reconstructed {@link SaleModel}, or {@code null} if input is null.
     */
    default SaleModel entityToModel(SaleEntity saleEntity) {
        if (saleEntity == null) {
            return null;
        }
        List<SaleLine> lines =
                saleEntity.getLines().stream()
                        .map(
                                line ->
                                        SaleLine.reconstruct(
                                                line.getId(),
                                                line.getProductId(),
                                                line.getProductCode(),
                                                line.getProductName(),
                                                line.getUnitPrice(),
                                                line.getQuantity()))
                        .toList();
        return SaleModel.reconstruct(
                saleEntity.getId(),
                saleEntity.getCashierId(),
                saleEntity.getStatus(),
                lines,
                saleEntity.getVersion(),
                saleEntity.getCreatedAt(),
                saleEntity.getUpdatedAt(),
                saleEntity.getClosedAt());
    }

    /**
     * Converts the aggregate into a JPA entity, keeping the scan order as {@code position}.
     *
     * @param saleModel aggregate to convert.
     * @return a {@link SaleEntity} with its lines, ready for persistence.
     */
    default SaleEntity modelToEntity(SaleModel saleModel) {
        SaleEntity saleEntity =
                SaleEntity.builder()
                        .id(saleModel.getId())
                        .cashierId(saleModel.getCashierId())
                        .status(saleModel.getStatus())
                        .version(saleModel.getVersion())
                        .createdAt(saleModel.getCreatedAt())
                        .updatedAt(saleModel.getUpdatedAt())
                        .closedAt(saleModel.getClosedAt())
                        .lines(new ArrayList<>())
                        .build();
        List<SaleLine> lines = saleModel.getLines();
        for (int position = 0; position < lines.size(); position++) {
            SaleLine line = lines.get(position);
            saleEntity
                    .getLines()
                    .add(
                            SaleLineEntity.builder()
                                    .id(line.getId())
                                    .sale(saleEntity)
                                    .productId(line.getProductId())
                                    .productCode(line.getProductCode())
                                    .productName(line.getProductName())
                                    .unitPrice(line.getUnitPrice())
                                    .quantity(line.getQuantity().value())
                                    .position(position)
                                    .build());
        }
        return saleEntity;
    }
}
