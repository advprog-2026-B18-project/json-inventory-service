package id.ac.ui.cs.advprog.jsoninventoryservice.exception;

import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(err -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", err.getField());
                    errorMap.put(MESSAGE_KEY, err.getDefaultMessage());
                    return errorMap;
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put(MESSAGE_KEY, "Validation Failed");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedAccessException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(ActiveOrderException.class)
    public ResponseEntity<Map<String, Object>> handleActiveOrder(ActiveOrderException ex) {
        Map<String, Object> response = buildBaseError(ex.getMessage());
        response.put("active_orders", ex.getActiveOrders());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryInUse(CategoryInUseException ex) {
        Map<String, Object> response = buildBaseError(ex.getMessage());
        response.put("product_count", ex.getProductCount());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(StockOperationException.class)
    public ResponseEntity<Map<String, Object>> handleStockOperation(StockOperationException ex) {
        return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode()), ex.getMessage(), null);
    }

    @ExceptionHandler({CannotAcquireLockException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ApiResponse<Object>> handleConcurrencyFailure(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("High traffic volume. Please try again in a moment.");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, List<String> errors) {
        Map<String, Object> response = buildBaseError(message);
        if (errors != null) response.put("errors", errors);
        return ResponseEntity.status(status).body(response);
    }

    private Map<String, Object> buildBaseError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}