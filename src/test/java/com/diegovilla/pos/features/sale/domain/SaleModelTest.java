package com.diegovilla.pos.features.sale.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.sale.domain.exceptions.EmptySaleException;
import com.diegovilla.pos.features.sale.domain.exceptions.InvalidQuantityException;
import com.diegovilla.pos.features.sale.domain.exceptions.SaleLineNotFoundException;
import com.diegovilla.pos.features.sale.domain.exceptions.SaleNotOpenException;
import com.diegovilla.pos.features.sale.domain.model.SaleLine;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SaleModelTest {

    private final UUID cashierId = UUID.randomUUID();
    private final ProductModel milk = product("8410001000011", "Leche entera 1L", "0.95");
    private final ProductModel bag = product("8410001000110", "Bolsa de papel", "0.10");

    private static ProductModel product(String code, String name, String price) {
        return ProductModel.reconstruct(
                UUID.randomUUID(), code, name, new BigDecimal(price), Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("Should create an open sale for the cashier without lines")
    void shouldCreateOpenSale() {
        SaleModel sale = SaleModel.create(cashierId);

        assertThat(sale.getCashierId()).isEqualTo(cashierId);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.OPEN);
        assertThat(sale.getLines()).isEmpty();
        assertThat(sale.getVersion()).isNull();
        assertThat(sale.getClosedAt()).isNull();
        assertThat(sale.total()).isEqualByComparingTo("0.00");
        assertThat(sale.itemCount()).isZero();
    }

    @Test
    @DisplayName("Should add a new line with a snapshot of the product price")
    void shouldAddLineWithPriceSnapshot() {
        SaleModel sale = SaleModel.create(cashierId);

        sale.addProduct(milk, 2);

        SaleLine line = sale.getLines().get(0);
        assertThat(line.getProductId()).isEqualTo(milk.getId());
        assertThat(line.getProductCode()).isEqualTo("8410001000011");
        assertThat(line.getProductName()).isEqualTo("Leche entera 1L");
        assertThat(line.getUnitPrice()).isEqualByComparingTo("0.95");
        assertThat(line.getQuantity().value()).isEqualTo(2);
        assertThat(line.subtotal()).isEqualByComparingTo("1.90");
    }

    @Test
    @DisplayName("Should increase the quantity when the same product is added again")
    void shouldIncreaseQuantityOfExistingLine() {
        SaleModel sale = SaleModel.create(cashierId);

        sale.addProduct(milk, 1);
        sale.addProduct(bag, 1);
        sale.addProduct(milk, 2);

        assertThat(sale.getLines()).hasSize(2);
        assertThat(sale.getLines().get(0).getQuantity().value()).isEqualTo(3);
        assertThat(sale.itemCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should reject a quantity above 999, also when adding to an existing line")
    void shouldRejectQuantityOutOfRange() {
        SaleModel sale = SaleModel.create(cashierId);
        sale.addProduct(milk, 998);

        assertThatThrownBy(() -> sale.addProduct(bag, 0))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("Quantity must be between 1 and 999.");
        assertThatThrownBy(() -> sale.addProduct(milk, 2))
                .isInstanceOf(InvalidQuantityException.class);
        assertThat(sale.getLines().get(0).getQuantity().value()).isEqualTo(998);
    }

    @Test
    @DisplayName("Should change the quantity of a line")
    void shouldChangeQuantity() {
        SaleModel sale = SaleModel.create(cashierId);
        sale.addProduct(milk, 1);
        UUID lineId = sale.getLines().get(0).getId();

        sale.changeQuantity(lineId, 5);

        assertThat(sale.getLines().get(0).getQuantity().value()).isEqualTo(5);
        assertThatThrownBy(() -> sale.changeQuantity(lineId, 1000))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("Should remove a line and keep the order of the others")
    void shouldRemoveLine() {
        SaleModel sale = SaleModel.create(cashierId);
        sale.addProduct(milk, 1);
        sale.addProduct(bag, 1);

        sale.removeLine(sale.getLines().get(0).getId());

        assertThat(sale.getLines())
                .extracting(SaleLine::getProductCode)
                .containsExactly("8410001000110");
    }

    @Test
    @DisplayName("Should throw SaleLineNotFoundException for a line of another sale")
    void shouldRejectUnknownLine() {
        SaleModel sale = SaleModel.create(cashierId);
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> sale.changeQuantity(unknown, 2))
                .isInstanceOf(SaleLineNotFoundException.class)
                .hasMessage("Sale line not found.");
        assertThatThrownBy(() -> sale.removeLine(unknown))
                .isInstanceOf(SaleLineNotFoundException.class);
    }

    @Test
    @DisplayName("Should complete a sale with lines and set closedAt")
    void shouldComplete() {
        SaleModel sale = SaleModel.create(cashierId);
        sale.addProduct(milk, 1);

        sale.complete();

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getClosedAt()).isNotNull().isEqualTo(sale.getUpdatedAt());
    }

    @Test
    @DisplayName("Should reject completing an empty sale")
    void shouldRejectCompletingEmptySale() {
        SaleModel sale = SaleModel.create(cashierId);

        assertThatThrownBy(sale::complete)
                .isInstanceOf(EmptySaleException.class)
                .hasMessage("An empty sale cannot be completed.");
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.OPEN);
    }

    @Test
    @DisplayName("Should cancel an open sale")
    void shouldCancel() {
        SaleModel sale = SaleModel.create(cashierId);

        sale.cancel();

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(sale.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should reject any change when the sale is not open")
    void shouldRejectChangesWhenNotOpen() {
        SaleModel sale = SaleModel.create(cashierId);
        sale.addProduct(milk, 1);
        UUID lineId = sale.getLines().get(0).getId();
        sale.complete();

        assertThatThrownBy(() -> sale.addProduct(bag, 1))
                .isInstanceOf(SaleNotOpenException.class)
                .hasMessage("The sale is not open.");
        assertThatThrownBy(() -> sale.changeQuantity(lineId, 2))
                .isInstanceOf(SaleNotOpenException.class);
        assertThatThrownBy(() -> sale.removeLine(lineId)).isInstanceOf(SaleNotOpenException.class);
        assertThatThrownBy(sale::complete).isInstanceOf(SaleNotOpenException.class);
        assertThatThrownBy(sale::cancel).isInstanceOf(SaleNotOpenException.class);
    }

    @Test
    @DisplayName("Should calculate the total with BigDecimal precision (0.10 x 3 = 0.30)")
    void shouldCalculateTotalWithPrecision() {
        SaleModel sale = SaleModel.create(cashierId);

        sale.addProduct(bag, 3);
        sale.addProduct(milk, 2);

        assertThat(sale.getLines().get(0).subtotal()).isEqualByComparingTo("0.30");
        assertThat(sale.total()).isEqualByComparingTo("2.20");
        assertThat(sale.total().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should reconstruct a sale and not expose its internal list")
    void shouldReconstruct() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        SaleLine line =
                SaleLine.reconstruct(
                        UUID.randomUUID(),
                        milk.getId(),
                        "8410001000011",
                        "Leche entera 1L",
                        new BigDecimal("0.95"),
                        2);

        SaleModel sale =
                SaleModel.reconstruct(
                        id, cashierId, SaleStatus.OPEN, List.of(line), 3L, now, now, null);

        assertThat(sale.getId()).isEqualTo(id);
        assertThat(sale.getVersion()).isEqualTo(3L);
        assertThat(sale.getLines()).containsExactly(line);
        assertThatThrownBy(() -> sale.getLines().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
