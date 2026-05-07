package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class LiveShoppingStrategy implements ShoppingModeStrategy {
    @Override
    public boolean isEligibleForReservation(Product product, int quantity) {
        return product.getStatus() == ProductStatus.ACTIVE && product.getStock() >= quantity;
    }
}