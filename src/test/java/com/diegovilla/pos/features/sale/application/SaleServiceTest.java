package com.diegovilla.pos.features.sale.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import com.diegovilla.pos.core.security.jwt.utils.PermissionValidator;
import com.diegovilla.pos.features.product.application.ports.ProductRepositoryPort;
import com.diegovilla.pos.features.product.domain.exceptions.ProductNotFoundException;
import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.sale.application.commands.SaleAddLineCommand;
import com.diegovilla.pos.features.sale.application.commands.SaleChangeQuantityCommand;
import com.diegovilla.pos.features.sale.application.ports.SaleRepositoryPort;
import com.diegovilla.pos.features.sale.application.services.SaleService;
import com.diegovilla.pos.features.sale.domain.exceptions.OpenSaleAlreadyExistsException;
import com.diegovilla.pos.features.sale.domain.exceptions.SaleNotFoundException;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    private static final String CODE = "8412345000010";

    @Mock private SaleRepositoryPort saleRepositoryPort;
    @Mock private ProductRepositoryPort productRepositoryPort;
    @Mock private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock private PermissionValidator permissionValidator;

    @InjectMocks private SaleService saleService;

    private final UUID cashierId = UUID.randomUUID();
    private final ProductModel milk =
            ProductModel.reconstruct(
                    UUID.randomUUID(),
                    CODE,
                    "Leche entera 1L",
                    new BigDecimal("0.95"),
                    Instant.now(),
                    Instant.now());

    private SaleModel givenOwnedSale() {
        SaleModel sale = SaleModel.create(cashierId);
        when(saleRepositoryPort.getById(sale.getId())).thenReturn(Optional.of(sale));
        return sale;
    }

    private void givenSaveReturnsArgument() {
        when(saleRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should open a sale for the authenticated cashier")
    void shouldOpen() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashierId);
        when(saleRepositoryPort.existsOpenByCashierId(cashierId)).thenReturn(false);
        givenSaveReturnsArgument();

        SaleModel sale = saleService.open();

        assertThat(sale.getCashierId()).isEqualTo(cashierId);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.OPEN);
    }

    @Test
    @DisplayName("Should throw OpenSaleAlreadyExistsException when the cashier has an open sale")
    void shouldRejectOpenWhenOneIsOpen() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashierId);
        when(saleRepositoryPort.existsOpenByCashierId(cashierId)).thenReturn(true);

        assertThatThrownBy(() -> saleService.open())
                .isInstanceOf(OpenSaleAlreadyExistsException.class)
                .hasMessage("The cashier already has an open sale.");
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should return the open sale of the cashier")
    void shouldGetCurrent() {
        SaleModel sale = SaleModel.create(cashierId);
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashierId);
        when(saleRepositoryPort.getOpenByCashierId(cashierId)).thenReturn(Optional.of(sale));

        assertThat(saleService.getCurrent()).isSameAs(sale);
    }

    @Test
    @DisplayName("Should throw SaleNotFoundException when the cashier has no open sale")
    void shouldThrowWhenNoCurrentSale() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashierId);
        when(saleRepositoryPort.getOpenByCashierId(cashierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getCurrent())
                .isInstanceOf(SaleNotFoundException.class)
                .hasMessage("Sale not found.");
    }

    @Test
    @DisplayName("Should return an owned sale by id")
    void shouldGetById() {
        SaleModel sale = givenOwnedSale();

        assertThat(saleService.getById(sale.getId())).isSameAs(sale);
        verify(permissionValidator).validateHasPermissions(cashierId);
    }

    @Test
    @DisplayName("Should throw SaleNotFoundException for an unknown sale")
    void shouldThrowWhenSaleNotFound() {
        UUID id = UUID.randomUUID();
        when(saleRepositoryPort.getById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getById(id)).isInstanceOf(SaleNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject a sale of another cashier with AccessDeniedException")
    void shouldRejectSaleOfAnotherCashier() {
        SaleModel sale = givenOwnedSale();
        doThrow(new AccessDeniedException("The resource belongs to another cashier"))
                .when(permissionValidator)
                .validateHasPermissions(cashierId);

        assertThatThrownBy(() -> saleService.addLine(sale.getId(), new SaleAddLineCommand(CODE, 1)))
                .isInstanceOf(AccessDeniedException.class);
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should add the scanned product to the sale and save it")
    void shouldAddLine() {
        SaleModel sale = givenOwnedSale();
        when(productRepositoryPort.getByCode(CODE)).thenReturn(Optional.of(milk));
        givenSaveReturnsArgument();

        SaleModel result = saleService.addLine(sale.getId(), new SaleAddLineCommand(CODE, 2));

        assertThat(result.getLines()).hasSize(1);
        assertThat(result.total()).isEqualByComparingTo("1.90");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when the scanned code does not exist")
    void shouldRejectUnknownProduct() {
        SaleModel sale = givenOwnedSale();
        when(productRepositoryPort.getByCode(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.addLine(sale.getId(), new SaleAddLineCommand(CODE, 1)))
                .isInstanceOf(ProductNotFoundException.class);
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should change the quantity of a line and save the sale")
    void shouldChangeLineQuantity() {
        SaleModel sale = givenOwnedSale();
        sale.addProduct(milk, 1);
        UUID lineId = sale.getLines().get(0).getId();
        givenSaveReturnsArgument();

        SaleModel result =
                saleService.changeLineQuantity(
                        sale.getId(), lineId, new SaleChangeQuantityCommand(4));

        assertThat(result.itemCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should remove a line and save the sale")
    void shouldRemoveLine() {
        SaleModel sale = givenOwnedSale();
        sale.addProduct(milk, 1);
        givenSaveReturnsArgument();

        SaleModel result = saleService.removeLine(sale.getId(), sale.getLines().get(0).getId());

        assertThat(result.getLines()).isEmpty();
    }

    @Test
    @DisplayName("Should complete a sale and save it")
    void shouldComplete() {
        SaleModel sale = givenOwnedSale();
        sale.addProduct(milk, 1);
        givenSaveReturnsArgument();

        assertThat(saleService.complete(sale.getId()).getStatus()).isEqualTo(SaleStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should cancel a sale and save it")
    void shouldCancel() {
        SaleModel sale = givenOwnedSale();

        saleService.cancel(sale.getId());

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        verify(saleRepositoryPort).save(sale);
    }
}
