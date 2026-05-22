package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ShoppingMode;

public interface ShoppingModeStrategy {
    boolean isEligibleForReservation(Product product, int quantity);
    ShoppingMode getSupportedMode();
}