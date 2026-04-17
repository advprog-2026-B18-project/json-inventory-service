package id.ac.ui.cs.advprog.jsoninventoryservice.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}