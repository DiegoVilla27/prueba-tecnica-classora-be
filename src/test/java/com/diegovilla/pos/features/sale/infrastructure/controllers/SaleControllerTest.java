package com.diegovilla.pos.features.sale.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.features.sale.application.commands.SaleAddLineCommand;
import com.diegovilla.pos.features.sale.application.commands.SaleChangeQuantityCommand;
import com.diegovilla.pos.features.sale.application.dto.response.SaleSummaryPage;
import com.diegovilla.pos.features.sale.application.services.SaleService;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import com.diegovilla.pos.features.sale.infrastructure.dto.request.SaleAddLineRequestDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.request.SaleChangeQuantityRequestDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SalePageResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.dto.response.SaleResponseDTO;
import com.diegovilla.pos.features.sale.infrastructure.mappers.SaleDtoMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock private SaleService saleService;
    @Mock private SaleDtoMapper saleDtoMapper;

    @InjectMocks private SaleController saleController;

    private final SaleModel sale = SaleModel.create(UUID.randomUUID());
    private final SaleResponseDTO dto =
            new SaleResponseDTO(
                    sale.getId(),
                    SaleStatus.OPEN,
                    sale.getCashierId(),
                    List.of(),
                    0,
                    new BigDecimal("0.00"),
                    0L,
                    sale.getCreatedAt(),
                    null);

    @BeforeEach
    void setUpRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/sales");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Should return 201 with the Location of the opened sale")
    void shouldOpen() {
        when(saleService.open()).thenReturn(sale);
        when(saleDtoMapper.modelToResponseDTO(sale)).thenReturn(dto);

        ResponseEntity<SaleResponseDTO> response = saleController.open();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation())
                .hasToString("http://localhost/api/v1/sales/" + sale.getId());
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should return 200 with the current sale and with a sale by id")
    void shouldGetCurrentAndById() {
        when(saleService.getCurrent()).thenReturn(sale);
        when(saleService.getById(sale.getId())).thenReturn(sale);
        when(saleDtoMapper.modelToResponseDTO(sale)).thenReturn(dto);

        assertThat(saleController.getCurrent().getBody()).isEqualTo(dto);
        assertThat(saleController.getById(sale.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should return 200 with the updated sale after line operations")
    void shouldHandleLineOperations() {
        UUID lineId = UUID.randomUUID();
        SaleAddLineCommand addCommand = new SaleAddLineCommand("8412345000010", 1);
        SaleChangeQuantityCommand changeCommand = new SaleChangeQuantityCommand(3);
        SaleAddLineRequestDTO addDto = new SaleAddLineRequestDTO("8412345000010", 1);
        SaleChangeQuantityRequestDTO changeDto = new SaleChangeQuantityRequestDTO(3);
        when(saleDtoMapper.addLineRequestDTOToCommand(addDto)).thenReturn(addCommand);
        when(saleDtoMapper.changeQuantityRequestDTOToCommand(changeDto)).thenReturn(changeCommand);
        when(saleService.addLine(sale.getId(), addCommand)).thenReturn(sale);
        when(saleService.changeLineQuantity(sale.getId(), lineId, changeCommand)).thenReturn(sale);
        when(saleService.removeLine(sale.getId(), lineId)).thenReturn(sale);
        when(saleDtoMapper.modelToResponseDTO(sale)).thenReturn(dto);

        assertThat(saleController.addLine(sale.getId(), addDto).getBody()).isEqualTo(dto);
        assertThat(saleController.changeLineQuantity(sale.getId(), lineId, changeDto).getBody())
                .isEqualTo(dto);
        assertThat(saleController.removeLine(sale.getId(), lineId).getBody()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should return 200 when completing and 204 when cancelling")
    void shouldCompleteAndCancel() {
        when(saleService.complete(sale.getId())).thenReturn(sale);
        when(saleDtoMapper.modelToResponseDTO(sale)).thenReturn(dto);

        assertThat(saleController.complete(sale.getId()).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> cancelled = saleController.cancel(sale.getId());

        assertThat(cancelled.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(saleService).cancel(sale.getId());
    }

    @Test
    @DisplayName("Should return 200 with a page of the sales history")
    void shouldListSales() {
        SaleSummaryPage page = new SaleSummaryPage(List.of(), 0, 20, 0, 0);
        SalePageResponseDTO pageDto = new SalePageResponseDTO(List.of(), 0, 20, 0, 0);
        when(saleService.listMine(0, 20)).thenReturn(page);
        when(saleDtoMapper.pageToResponseDTO(page)).thenReturn(pageDto);

        ResponseEntity<SalePageResponseDTO> response = saleController.list(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pageDto);
    }
}
