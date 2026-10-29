package com.diegovilla.pos.features.sale.infrastructure.adapters;

import com.diegovilla.pos.core.errors.translators.DatabaseExceptionTranslator;
import com.diegovilla.pos.features.sale.application.dto.response.SaleSummary;
import com.diegovilla.pos.features.sale.application.dto.response.SaleSummaryPage;
import com.diegovilla.pos.features.sale.application.ports.SaleRepositoryPort;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import com.diegovilla.pos.features.sale.infrastructure.mappers.SaleEntityMapper;
import com.diegovilla.pos.features.sale.infrastructure.repository.SaleJpaRepository;
import com.diegovilla.pos.features.sale.infrastructure.repository.SaleSummaryRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementing {@link SaleRepositoryPort} with Spring Data JPA.
 *
 * <p>Saving merges the aggregate with its lines; a stale {@code version} raises {@link
 * org.springframework.orm.ObjectOptimisticLockingFailureException} (409, RN-10).
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements SaleRepositoryPort {

    private final SaleJpaRepository saleJpaRepository;
    private final SaleEntityMapper saleEntityMapper;
    private final DatabaseExceptionTranslator databaseExceptionTranslator;

    /** {@inheritDoc} */
    @Override
    public Optional<SaleModel> getById(UUID id) {
        return saleJpaRepository.findById(id).map(saleEntityMapper::entityToModel);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<SaleModel> getOpenByCashierId(UUID cashierId) {
        return saleJpaRepository
                .findByCashierIdAndStatus(cashierId, SaleStatus.OPEN)
                .map(saleEntityMapper::entityToModel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsOpenByCashierId(UUID cashierId) {
        return saleJpaRepository.existsByCashierIdAndStatus(cashierId, SaleStatus.OPEN);
    }

    /** {@inheritDoc} */
    @Override
    public SaleSummaryPage findSummariesByCashierId(UUID cashierId, int page, int size) {
        Page<SaleSummaryRow> rows =
                saleJpaRepository.findSummariesByCashierId(cashierId, PageRequest.of(page, size));
        return new SaleSummaryPage(
                rows.map(SaleRepositoryAdapter::toSummary).getContent(),
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages());
    }

    /** Sums over no lines are {@code null}: an empty sale has 0 items and a 0.00 total. */
    private static SaleSummary toSummary(SaleSummaryRow row) {
        BigDecimal total = row.total() == null ? BigDecimal.ZERO : row.total();
        return new SaleSummary(
                row.id(),
                row.status(),
                row.itemCount() == null ? 0 : row.itemCount().intValue(),
                row.lineCount().intValue(),
                total.setScale(2, RoundingMode.HALF_EVEN),
                row.createdAt(),
                row.closedAt());
    }

    /** {@inheritDoc} */
    @Override
    public SaleModel save(SaleModel saleModel) {
        try {
            return saleEntityMapper.entityToModel(
                    saleJpaRepository.saveAndFlush(saleEntityMapper.modelToEntity(saleModel)));
        } catch (DataIntegrityViolationException ex) {
            throw databaseExceptionTranslator.translate(ex);
        }
    }
}
