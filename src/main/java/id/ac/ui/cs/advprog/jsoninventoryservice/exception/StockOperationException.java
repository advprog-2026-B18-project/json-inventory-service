package id.ac.ui.cs.advprog.jsoninventoryservice.exception;

import lombok.Getter;

@Getter
public class StockOperationException extends RuntimeException {
    private final int statusCode;
    public StockOperationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}