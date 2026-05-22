package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.exception.StockOperationException;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ProductStatus;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FlashSaleStrategy implements ShoppingModeStrategy {
    @Override
    public boolean isEligibleForReservation(Product product, int quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new StockOperationException("This product is currently unavailable.", 400);
        }
        if (product.getStock() < quantity) {
            throw new StockOperationException("Insufficient stock of Flash Sale items.", 400);
        }
        if (product.getFlashSaleStart() == null || product.getFlashSaleEnd() == null) {
            throw new StockOperationException("Invalid Flash Sale configuration.", 400);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(product.getFlashSaleStart())) {
            throw new StockOperationException("The flash sale hasn't started yet.", 400);
        }
        if (now.isAfter(product.getFlashSaleEnd())) {
            throw new StockOperationException("The flash sale is over.", 400);
        }
        return true;
    }

    @Override
    public ShoppingMode getSupportedMode() {
        return ShoppingMode.FLASH_SALE;
    }
}