package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PreOrderStrategy implements ShoppingModeStrategy {
    @Override
    public boolean isEligibleForReservation(Product product, int quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new StockOperationException("This product is currently unavailable.", 400);
        }
        if (product.getStock() < quantity) {
            throw new StockOperationException("Insufficient quota available.", 400);
        }
        if (product.getPurchaseDate() != null && product.getPurchaseDate().isBefore(LocalDate.now())) {
            throw new StockOperationException("Pre-order closed. Items are being purchased.", 400);
        }
        return true;
    }

    @Override
    public ShoppingMode getSupportedMode() {
        return ShoppingMode.PRE_ORDER;
    }
}