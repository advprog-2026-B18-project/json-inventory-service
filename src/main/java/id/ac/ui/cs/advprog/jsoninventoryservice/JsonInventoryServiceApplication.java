package id.ac.ui.cs.advprog.jsoninventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JsonInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonInventoryServiceApplication.class, args);
    }

}
