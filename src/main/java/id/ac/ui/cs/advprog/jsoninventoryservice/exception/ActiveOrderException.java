package id.ac.ui.cs.advprog.jsoninventoryservice.exception;

import lombok.Getter;

@Getter
public class ActiveOrderException extends RuntimeException {
    private final long activeOrders;
    public ActiveOrderException(long activeOrders) {
        super("Cannot delete product. There are active orders (PENDING/CONFIRMED).");
        this.activeOrders = activeOrders;
    }
}