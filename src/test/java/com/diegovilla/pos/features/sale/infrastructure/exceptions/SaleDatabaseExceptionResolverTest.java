package com.diegovilla.pos.features.sale.infrastructure.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.diegovilla.pos.features.sale.domain.exceptions.ConcurrentSaleModificationException;
import com.diegovilla.pos.features.sale.domain.exceptions.OpenSaleAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SaleDatabaseExceptionResolverTest {

    private final SaleDatabaseExceptionResolver resolver = new SaleDatabaseExceptionResolver();

    @Test
    @DisplayName("Should translate the one-open-sale index into OpenSaleAlreadyExistsException")
    void shouldResolveOpenSaleIndex() {
        assertThat(resolver.resolve("ux_sales_one_open_per_cashier"))
                .isInstanceOf(OpenSaleAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should translate the one-line-per-product constraint into a 409 conflict")
    void shouldResolveDuplicatedLine() {
        assertThat(resolver.resolve("uk_sale_lines_sale_product"))
                .isInstanceOf(ConcurrentSaleModificationException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should ignore constraints of other tables")
    void shouldIgnoreOtherConstraints() {
        assertThat(resolver.resolve("uk_users_email")).isNull();
    }
}
