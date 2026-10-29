package com.diegovilla.pos.features.sale.infrastructure.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.errors.translators.DatabaseExceptionTranslator;
import com.diegovilla.pos.features.sale.application.dto.response.SaleSummaryPage;
import com.diegovilla.pos.features.sale.domain.exceptions.OpenSaleAlreadyExistsException;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import com.diegovilla.pos.features.sale.infrastructure.entity.SaleEntity;
import com.diegovilla.pos.features.sale.infrastructure.mappers.SaleEntityMapper;
import com.diegovilla.pos.features.sale.infrastructure.repository.SaleJpaRepository;
import com.diegovilla.pos.features.sale.infrastructure.repository.SaleSummaryRow;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SaleRepositoryAdapterTest {

    @Mock private SaleJpaRepository saleJpaRepository;
    @Mock private SaleEntityMapper saleEntityMapper;
    @Mock private DatabaseExceptionTranslator databaseExceptionTranslator;

    @InjectMocks private SaleRepositoryAdapter adapter;

    private final UUID cashierId = UUID.randomUUID();
    private final SaleModel sale = SaleModel.create(cashierId);
    private final SaleEntity entity = new SaleEntity();

    @Test
    @DisplayName("Should map the sale found by id and the open sale of a cashier")
    void shouldFindByIdAndOpenByCashier() {
        when(saleJpaRepository.findById(sale.getId())).thenReturn(Optional.of(entity));
        when(saleJpaRepository.findByCashierIdAndStatus(cashierId, SaleStatus.OPEN))
                .thenReturn(Optional.of(entity));
        when(saleEntityMapper.entityToModel(entity)).thenReturn(sale);

        assertThat(adapter.getById(sale.getId())).contains(sale);
        assertThat(adapter.getOpenByCashierId(cashierId)).contains(sale);
    }

    @Test
    @DisplayName("Should delegate the open-sale existence check")
    void shouldCheckOpenSaleExists() {
        when(saleJpaRepository.existsByCashierIdAndStatus(cashierId, SaleStatus.OPEN))
                .thenReturn(true);

        assertThat(adapter.existsOpenByCashierId(cashierId)).isTrue();
    }

    @Test
    @DisplayName("Should save and map back the persisted sale")
    void shouldSave() {
        when(saleEntityMapper.modelToEntity(sale)).thenReturn(entity);
        when(saleJpaRepository.saveAndFlush(entity)).thenReturn(entity);
        when(saleEntityMapper.entityToModel(entity)).thenReturn(sale);

        assertThat(adapter.save(sale)).isSameAs(sale);
    }

    @Test
    @DisplayName("Should translate integrity violations on save")
    void shouldTranslateIntegrityViolation() {
        OpenSaleAlreadyExistsException translated = new OpenSaleAlreadyExistsException();
        when(saleEntityMapper.modelToEntity(sale)).thenReturn(entity);
        when(saleJpaRepository.saveAndFlush(entity))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(databaseExceptionTranslator.translate(any())).thenReturn(translated);

        assertThatThrownBy(() -> adapter.save(sale)).isSameAs(translated);
    }

    @Test
    @DisplayName("Should map the history rows, turning the sums of an empty sale into zeros")
    void shouldMapSummaryPage() {
        UUID cashierId = UUID.randomUUID();
        Instant now = Instant.now();
        SaleSummaryRow withLines =
                new SaleSummaryRow(
                        UUID.randomUUID(),
                        SaleStatus.COMPLETED,
                        5L,
                        2L,
                        new BigDecimal("2.2"),
                        now,
                        now);
        SaleSummaryRow empty =
                new SaleSummaryRow(UUID.randomUUID(), SaleStatus.OPEN, null, 0L, null, now, null);
        when(saleJpaRepository.findSummariesByCashierId(cashierId, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(withLines, empty), PageRequest.of(0, 2), 3));

        SaleSummaryPage page = adapter.findSummariesByCashierId(cashierId, 0, 2);

        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.content().get(0).total()).isEqualByComparingTo("2.20");
        assertThat(page.content().get(0).total().scale()).isEqualTo(2);
        assertThat(page.content().get(1).itemCount()).isZero();
        assertThat(page.content().get(1).total()).isEqualTo(new BigDecimal("0.00"));
    }
}
