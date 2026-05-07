package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShoppingModeProvider {
    private final LiveShoppingStrategy liveShoppingStrategy;
    private final PreOrderStrategy preOrderStrategy;
    private final FlashSaleStrategy flashSaleStrategy;

    public ShoppingModeStrategy getStrategy(ShoppingMode mode) {
        if (mode == null) return liveShoppingStrategy;

        return switch (mode) {
            case LIVE -> liveShoppingStrategy;
            case PRE_ORDER -> preOrderStrategy;
            case FLASH_SALE -> flashSaleStrategy;
        };
    }
}