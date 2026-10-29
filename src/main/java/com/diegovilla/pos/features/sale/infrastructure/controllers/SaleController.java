package com.diegovilla.pos.features.sale.infrastructure.controllers;

import com.diegovilla.pos.features.sale.application.services.SaleService;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.infrastructure.docs.AddSaleLineDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.CancelSaleDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.ChangeSaleLineQuantityDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.CompleteSaleDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.GetCurrentSaleDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.GetSaleByIdDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.ListSalesDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.OpenSaleDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.docs.RemoveSaleLineDocumentation;
import com.diegovilla.pos.features.sale.infrastructure.dto.request.SaleAddLineRequestDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.request.SaleChangeQuantityRequestDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SalePageResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SaleResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.mappers.SaleDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller exposing the checkout operations on the sales of the authenticated cashier.
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Checkout operations on the sales of the authenticated cashier.")
public class SaleController {

    private final SaleService saleService;
    private final SaleDtoMapper saleDtoMapper;

    /**
     * Opens a new sale for the authenticated cashier.
     *
     * @return HTTP 201 with the new {@link SaleResponseDTO} and its {@code Location}.
     */
    @PostMapping
    @OpenSaleDocumentation
    public ResponseEntity<SaleResponseDTO> open() {
        SaleModel sale = saleService.open();
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(sale.getId())
                        .toUri();
        return ResponseEntity.created(location).body(saleDtoMapper.modelToResponseDTO(sale));
    }

    /**
     * Lists the sales of the authenticated cashier, newest first.
     *
     * @param page zero-based page number.
     * @param size page size (1–100).
     * @return HTTP 200 with the {@link SalePageResponseDTO}.
     */
    @GetMapping
    @ListSalesDocumentation
    public ResponseEntity<SalePageResponseDTO> list(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be 0 or more")
                    int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "Size must be between 1 and 100")
                    @Max(value = 100, message = "Size must be between 1 and 100")
                    int size) {
        return ResponseEntity.ok(saleDtoMapper.pageToResponseDTO(saleService.listMine(page, size)));
    }

    /**
     * Retrieves the open sale of the authenticated cashier.
     *
     * @return HTTP 200 with the open {@link SaleResponseDTO}.
     */
    @GetMapping("/current")
    @GetCurrentSaleDocumentation
    public ResponseEntity<SaleResponseDTO> getCurrent() {
        return ResponseEntity.ok(saleDtoMapper.modelToResponseDTO(saleService.getCurrent()));
    }

    /**
     * Retrieves a sale of the authenticated cashier.
     *
     * @param id identifier of the sale.
     * @return HTTP 200 with the {@link SaleResponseDTO}.
     */
    @GetMapping("/{id}")
    @GetSaleByIdDocumentation
    public ResponseEntity<SaleResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(saleDtoMapper.modelToResponseDTO(saleService.getById(id)));
    }

    /**
     * Adds a product to a sale by its code.
     *
     * @param id identifier of the sale.
     * @param dto validated product code and units.
     * @return HTTP 200 with the updated {@link SaleResponseDTO}.
     */
    @PostMapping("/{id}/lines")
    @AddSaleLineDocumentation
    public ResponseEntity<SaleResponseDTO> addLine(
            @PathVariable UUID id, @Valid @RequestBody SaleAddLineRequestDTO dto) {
        return ResponseEntity.ok(
                saleDtoMapper.modelToResponseDTO(
                        saleService.addLine(id, saleDtoMapper.addLineRequestDTOToCommand(dto))));
    }

    /**
     * Replaces the quantity of a sale line.
     *
     * @param id identifier of the sale.
     * @param lineId identifier of the line.
     * @param dto validated new units.
     * @return HTTP 200 with the updated {@link SaleResponseDTO}.
     */
    @PatchMapping("/{id}/lines/{lineId}")
    @ChangeSaleLineQuantityDocumentation
    public ResponseEntity<SaleResponseDTO> changeLineQuantity(
            @PathVariable UUID id,
            @PathVariable UUID lineId,
            @Valid @RequestBody SaleChangeQuantityRequestDTO dto) {
        return ResponseEntity.ok(
                saleDtoMapper.modelToResponseDTO(
                        saleService.changeLineQuantity(
                                id, lineId, saleDtoMapper.changeQuantityRequestDTOToCommand(dto))));
    }

    /**
     * Removes a line from a sale.
     *
     * @param id identifier of the sale.
     * @param lineId identifier of the line.
     * @return HTTP 200 with the updated {@link SaleResponseDTO}.
     */
    @DeleteMapping("/{id}/lines/{lineId}")
    @RemoveSaleLineDocumentation
    public ResponseEntity<SaleResponseDTO> removeLine(
            @PathVariable UUID id, @PathVariable UUID lineId) {
        return ResponseEntity.ok(
                saleDtoMapper.modelToResponseDTO(saleService.removeLine(id, lineId)));
    }

    /**
     * Completes (charges) a sale.
     *
     * @param id identifier of the sale.
     * @return HTTP 200 with the completed {@link SaleResponseDTO}.
     */
    @PatchMapping("/{id}/complete")
    @CompleteSaleDocumentation
    public ResponseEntity<SaleResponseDTO> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(saleDtoMapper.modelToResponseDTO(saleService.complete(id)));
    }

    /**
     * Cancels (empties) a sale.
     *
     * @param id identifier of the sale.
     * @return HTTP 204 without body.
     */
    @PatchMapping("/{id}/cancel")
    @CancelSaleDocumentation
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        saleService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
