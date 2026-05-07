package id.ac.ui.cs.advprog.jsoninventoryservice.strategy;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.Product;

public interface ShoppingModeStrategy {
    boolean isEligibleForReservation(Product product, int quantity);
}