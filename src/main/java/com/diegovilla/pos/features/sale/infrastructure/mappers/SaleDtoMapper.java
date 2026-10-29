package com.diegovilla.pos.features.sale.infrastructure.mappers;

import com.diegovilla.pos.features.sale.application.commands.SaleAddLineCommand;
import com.diegovilla.pos.features.sale.application.commands.SaleChangeQuantityCommand;
import com.diegovilla.pos.features.sale.application.dto.response.SaleSummary;
import com.diegovilla.pos.features.sale.application.dto.response.SaleSummaryPage;
import com.diegovilla.pos.features.sale.domain.model.SaleLine;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.infrastructure.dto.request.SaleAddLineRequestDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.request.SaleChangeQuantityRequestDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SaleLineResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SalePageResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SaleResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SaleSummaryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between sale DTOs, commands and the aggregate.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SaleDtoMapper {

    /**
     * Converts an add-line request into a command.
     *
     * @param dto validated request.
     * @return the {@link SaleAddLineCommand}.
     */
    SaleAddLineCommand addLineRequestDTOToCommand(SaleAddLineRequestDTO dto);

    /**
     * Converts a change-quantity request into a command.
     *
     * @param dto validated request.
     * @return the {@link SaleChangeQuantityCommand}.
     */
    SaleChangeQuantityCommand changeQuantityRequestDTOToCommand(SaleChangeQuantityRequestDTO dto);

    /**
     * Converts the aggregate into its response, with the derived totals.
     *
     * @param saleModel sale to convert.
     * @return the {@link SaleResponseDTO}.
     */
    @Mapping(target = "itemCount", expression = "java(saleModel.itemCount())")
    @Mapping(target = "total", expression = "java(saleModel.total())")
    SaleResponseDTO modelToResponseDTO(SaleModel saleModel);

    /**
     * Converts a line into its response.
     *
     * @param saleLine line to convert.
     * @return the {@link SaleLineResponseDTO}.
     */
    @Mapping(target = "code", source = "productCode")
    @Mapping(target = "name", source = "productName")
    @Mapping(target = "quantity", source = "quantity.value")
    @Mapping(target = "subtotal", expression = "java(saleLine.subtotal())")
    SaleLineResponseDTO lineToResponseDTO(SaleLine saleLine);

    /**
     * Converts one page of the sales history into its response.
     *
     * @param page summaries page.
     * @return the {@link SalePageResponseDTO}.
     */
    SalePageResponseDTO pageToResponseDTO(SaleSummaryPage page);

    /**
     * Converts a sale summary into its response.
     *
     * @param summary sale summary.
     * @return the {@link SaleSummaryResponseDTO}.
     */
    SaleSummaryResponseDTO summaryToResponseDTO(SaleSummary summary);
}
