package id.ac.ui.cs.advprog.jsoninventoryservice.exception;

import lombok.Getter;

@Getter
public class CategoryInUseException extends RuntimeException {
    private final int productCount;
    public CategoryInUseException(int productCount) {
        super("Cannot delete category. It is currently used by active products.");
        this.productCount = productCount;
    }
}