package id.ac.ui.cs.advprog.jsoninventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableScheduling
@SpringBootApplication
@EnableMethodSecurity
@EnableCaching
public class JsonInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonInventoryServiceApplication.class, args);
    }

}
