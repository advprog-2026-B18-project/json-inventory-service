package id.ac.ui.cs.advprog.jsoninventoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class JsonInventoryServiceApplicationTests {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            // context load check
        });
    }

    @Test
    void testMain() {
        JsonInventoryServiceApplication.main(new String[] {"--server.port=0"});
    }

}
