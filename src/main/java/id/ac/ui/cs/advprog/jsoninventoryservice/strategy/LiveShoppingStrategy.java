package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class LiveShoppingStrategy implements ShoppingModeStrategy {

    @Override
    public boolean isEligibleForReservation(Product product, int quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new StockOperationException("This product is currently unavailable.", 400);
        }
        if (product.getStock() < quantity) {
            throw new StockOperationException("Insufficient stock.", 400);
        }

        return true;
    }
}