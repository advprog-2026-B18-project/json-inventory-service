package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ShoppingModeProvider {
    private final Map<ShoppingMode, ShoppingModeStrategy> strategyMap;
    private final LiveShoppingStrategy defaultStrategy;

    public ShoppingModeProvider(List<ShoppingModeStrategy> strategies, LiveShoppingStrategy defaultStrategy) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(ShoppingModeStrategy::getSupportedMode, Function.identity()));
        this.defaultStrategy = defaultStrategy;
    }

    public ShoppingModeStrategy getStrategy(ShoppingMode mode) {
        if (mode == null) return defaultStrategy;
        return strategyMap.getOrDefault(mode, defaultStrategy);
    }
}