package id.ac.ui.cs.advprog.jsoninventoryservice.exception;

import id.ac.ui.cs.advprog.jsoninventoryservice.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("objectName", "price", "Must be positive");
        FieldError fieldError2 = new FieldError("objectName", "name", "Must not be empty");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) response.getBody().get("errors");
        assertEquals(2, errors.size());
        assertEquals("price", errors.getFirst().get("field"));
        assertEquals("Must be positive", errors.getFirst().get("message"));
    }

    @Test
    void handleUnauthorized_Success() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Unauthorized");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUnauthorized(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleDuplicate_Success() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDuplicate(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleCategoryInUse_Success() {
        CategoryInUseException ex = new CategoryInUseException(5);
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleCategoryInUse(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(5, response.getBody().get("product_count"));
    }

    @Test
    void handleNotFound_Success() {
        IllegalArgumentException ex = new IllegalArgumentException("Not found");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testHandleUnauthorized_HitBuildErrorResponseBranch() {UnauthorizedAccessException ex = new UnauthorizedAccessException("Unauthorized");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleUnauthorized(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleValidationErrors_EmptyErrors() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);

        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Object errors = response.getBody().get("errors");
        assertTrue(errors == null || ((List<?>) errors).isEmpty());
    }

    @Test
    void testHandleNotFound_NullErrorsBranch() {
        IllegalArgumentException ex = new IllegalArgumentException("Not found exception");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get("errors"));
    }

    @Test
    void testBuildErrorResponse_BothBranches() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Method method = GlobalExceptionHandler.class.getDeclaredMethod("buildErrorResponse", HttpStatus.class, String.class, List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> trueResponse =
                (ResponseEntity<Map<String, Object>>)
                        method.invoke(handler, HttpStatus.BAD_REQUEST, "Test True", List.of("Error 1"));

        assertNotNull(trueResponse.getBody().get("errors"));

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> falseResponse =
                (ResponseEntity<Map<String, Object>>)
                        method.invoke(handler, HttpStatus.BAD_REQUEST, "Test False", null);

        assertNull(falseResponse.getBody().get("errors"));
    }

    @Test
    void testHandleConcurrencyFailure_CannotAcquireLockException() {
        CannotAcquireLockException exception = new CannotAcquireLockException("Lock wait timeout exceeded");
        ResponseEntity<ApiResponse<Object>> responseEntity = exceptionHandler.handleConcurrencyFailure(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("High traffic volume. Please try again in a moment.", responseEntity.getBody().getMessage());
    }

    @Test
    void testHandleConcurrencyFailure_PessimisticLockingFailureException() {
        PessimisticLockingFailureException exception = new PessimisticLockingFailureException("Pessimistic locking failed");
        ResponseEntity<ApiResponse<Object>> responseEntity = exceptionHandler.handleConcurrencyFailure(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isSuccess());
        assertEquals("High traffic volume. Please try again in a moment.", responseEntity.getBody().getMessage());
    }
}