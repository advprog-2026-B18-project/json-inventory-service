package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PreOrderStrategy implements ShoppingModeStrategy {
    @Override
    public boolean isEligibleForReservation(Product product, int quantity) {
        if (product.getPurchaseDate() != null && product.getPurchaseDate().isBefore(LocalDate.now())) {
            return false;
        }
        return product.getStatus() == ProductStatus.ACTIVE && product.getStock() >= quantity;
    }
}