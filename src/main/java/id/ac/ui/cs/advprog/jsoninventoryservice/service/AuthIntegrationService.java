package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import java.util.UUID;

public interface AuthIntegrationService {
    UUID getJastiperIdByUsername(String username);
}