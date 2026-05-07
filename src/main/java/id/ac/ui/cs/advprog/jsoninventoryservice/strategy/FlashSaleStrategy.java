package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FlashSaleStrategy implements ShoppingModeStrategy {
    @Override
    public boolean isEligibleForReservation(Product product, int quantity) {
        LocalDateTime now = LocalDateTime.now();
        if (product.getFlashSaleStart() == null || product.getFlashSaleEnd() == null) {
            return false;
        }
        if (now.isBefore(product.getFlashSaleStart())) {
            throw new StockOperationException("Flash sale has not started yet.", 400);
        }
        if (now.isAfter(product.getFlashSaleEnd())) {
            throw new StockOperationException("Flash sale has ended.", 400);
        }

        return product.getStatus() == ProductStatus.ACTIVE && product.getStock() >= quantity;
    }
}