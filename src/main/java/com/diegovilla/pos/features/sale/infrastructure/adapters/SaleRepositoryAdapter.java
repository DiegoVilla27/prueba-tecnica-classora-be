package com.diegovilla.pos.features.sale.infrastructure.adapters;

import com.diegovilla.pos.core.errors.translators.DatabaseExceptionTranslator;
import com.diegovilla.pos.features.sale.application.ports.SaleRepositoryPort;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import com.diegovilla.pos.features.sale.infrastructure.mappers.SaleEntityMapper;
import com.diegovilla.pos.features.sale.infrastructure.repository.SaleJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    public SaleModel save(SaleModel saleModel) {
        try {
            return saleEntityMapper.entityToModel(
                    saleJpaRepository.saveAndFlush(saleEntityMapper.modelToEntity(saleModel)));
        } catch (DataIntegrityViolationException ex) {
            throw databaseExceptionTranslator.translate(ex);
        }
    }
}
