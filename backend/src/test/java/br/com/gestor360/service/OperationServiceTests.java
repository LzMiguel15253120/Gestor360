package br.com.gestor360.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.gestor360.dto.OperationDtos.SaleItemRequest;
import br.com.gestor360.dto.OperationDtos.SaleRequest;
import br.com.gestor360.entity.Product;
import br.com.gestor360.exception.BusinessRuleException;
import br.com.gestor360.repository.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTests {
    @Mock ProductRepository products;
    @Mock CustomerRepository customers;
    @Mock SupplierRepository suppliers;
    @Mock SaleRepository sales;
    @Mock PurchaseRepository purchases;
    @Mock StockMovementRepository movements;
    @Mock AccountRepository accounts;
    @Mock PaymentRepository payments;
    @InjectMocks OperationService service;

    @Test
    void saleWithInsufficientStockDoesNotCreateSaleOrFinancialEntries() {
        Product product = mock(Product.class);
        when(products.findById(7L)).thenReturn(Optional.of(product));
        when(product.isActive()).thenReturn(true);
        when(product.getId()).thenReturn(7L);
        when(product.getName()).thenReturn("Caderno");
        when(products.reduceStock(7L, 2)).thenReturn(0);

        SaleRequest request = new SaleRequest(null, List.of(new SaleItemRequest(7L, 2)), "PIX", null);
        assertThrows(BusinessRuleException.class, () -> service.createSale(request));

        verify(sales, never()).saveAndFlush(any());
        verify(movements, never()).save(any());
        verify(accounts, never()).save(any());
    }

    @Test
    void saleRejectsUnknownPaymentMethod() {
        SaleRequest request = new SaleRequest(null, List.of(new SaleItemRequest(7L, 1)), "CRIPTO", null);
        assertThrows(BusinessRuleException.class, () -> service.createSale(request));
        verifyNoInteractions(products, customers, sales, movements, accounts);
    }
}
