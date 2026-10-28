package com.diegovilla.pos.features.sale.infrastructure.entity;

import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the {@code sales} table (schema defined by Flyway {@code V1}).
 *
 * <p>Its lines are saved and removed with it ({@code cascade = ALL}, {@code orphanRemoval}).
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sales")
public class SaleEntity {

    /** Unique primary key identifier. */
    @Id private UUID id;

    /** Cashier who owns the sale. */
    @Column(name = "cashier_id", nullable = false, updatable = false)
    private UUID cashierId;

    /** Lifecycle status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status;

    /** Optimistic-locking version ({@code null} while not persisted). */
    @Version private Long version;

    /** Lines in scan order. */
    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SaleLineEntity> lines = new ArrayList<>();

    /** Timestamp when the record was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp when the record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Timestamp when the sale was completed or cancelled. */
    @Column(name = "closed_at")
    private Instant closedAt;
}
