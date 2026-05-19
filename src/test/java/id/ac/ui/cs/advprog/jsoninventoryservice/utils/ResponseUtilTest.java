package id.ac.ui.cs.advprog.jsoninventoryservice.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ResponseUtilTest {

    @Test
    void testSuccessResponse() {
        String data = "Dummy Data";
        String message = "Success Message";

        ResponseEntity<ApiResponse<String>> response = ResponseUtil.success(data, message);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void testCreatedResponse() {
        String data = "Dummy Data";
        String message = "Created Message";

        ResponseEntity<ApiResponse<String>> response = ResponseUtil.created(data, message);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void testNotFoundResponse() {
        String message = "Not Found Message";

        ResponseEntity<ApiResponse<String>> response = ResponseUtil.notFound(message);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<ResponseUtil> constructor = ResponseUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ResponseUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}