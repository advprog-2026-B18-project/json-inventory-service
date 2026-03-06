package id.ac.ui.cs.advprog.jsoninventoryservice.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testLombokMethods() {
        ApiResponse<String> res1 = new ApiResponse<>();
        res1.setSuccess(true);
        res1.setMessage("Ok");
        res1.setData("Data");

        ApiResponse<String> res2 = new ApiResponse<>(true, "Ok", "Data");

        assertEquals(res1, res2);
        assertEquals(res1.hashCode(), res2.hashCode());
        assertNotNull(res1.toString());
        assertTrue(res1.isSuccess());
        assertEquals("Ok", res1.getMessage());
        assertEquals("Data", res1.getData());
    }
}