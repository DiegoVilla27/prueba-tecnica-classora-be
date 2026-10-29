package com.diegovilla.pos.features.sale.infrastructure.repository;

import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import com.diegovilla.pos.features.sale.infrastructure.entity.SaleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link SaleEntity}.
 *
 * <p>Sale reads load the lines in the same query ({@link EntityGraph}), since the aggregate is
 * always used with them.
 *
 * @since 1.0.0
 */
@Repository
public interface SaleJpaRepository extends JpaRepository<SaleEntity, UUID> {

    /**
     * Finds a sale by id together with its lines.
     *
     * @param id identifier of the sale.
     * @return an {@link Optional} containing the entity if found.
     */
    @Override
    @EntityGraph(attributePaths = "lines")
    Optional<SaleEntity> findById(UUID id);

    /**
     * Finds the sale of a cashier in the given status.
     *
     * @param cashierId identifier of the cashier.
     * @param status status to match (used with {@code OPEN}, unique per cashier).
     * @return an {@link Optional} containing the entity if found.
     */
    @EntityGraph(attributePaths = "lines")
    Optional<SaleEntity> findByCashierIdAndStatus(UUID cashierId, SaleStatus status);

    /**
     * Checks whether a cashier has a sale in the given status.
     *
     * @param cashierId identifier of the cashier.
     * @param status status to match.
     * @return {@code true} if found; {@code false} otherwise.
     */
    boolean existsByCashierIdAndStatus(UUID cashierId, SaleStatus status);

    /**
     * Retrieves one page of the sales of a cashier, newest first, with their lines aggregated in
     * the same query (no lines are loaded).
     *
     * @param cashierId identifier of the cashier.
     * @param pageable page number and size (the order is fixed by the query).
     * @return a page of {@link SaleSummaryRow}.
     */
    @Query(
            value =
                    """
                    select new com.diegovilla.pos.features.sale.infrastructure.repository.SaleSummaryRow(
                        s.id, s.status, sum(l.quantity), count(l),
                        sum(l.unitPrice * l.quantity), s.createdAt, s.closedAt)
                    from SaleEntity s left join s.lines l
                    where s.cashierId = :cashierId
                    group by s.id, s.status, s.createdAt, s.closedAt
                    order by s.createdAt desc
                    """,
            countQuery = "select count(s) from SaleEntity s where s.cashierId = :cashierId")
    Page<SaleSummaryRow> findSummariesByCashierId(
            @Param("cashierId") UUID cashierId, Pageable pageable);
}
