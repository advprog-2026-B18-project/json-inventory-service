package id.ac.ui.cs.advprog.jsoninventoryservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthIntegrationServiceImpl implements AuthIntegrationService {
    @Value("${modul1.auth.url:http://localhost:8082}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;

    public AuthIntegrationServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public UUID getJastiperIdByUsername(String username) {
        String url = authServiceUrl + "/profile/" + username;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("success")) && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data.containsKey("status") && "BANNED".equalsIgnoreCase(String.valueOf(data.get("status")))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This Jastiper is not available (BANNED).");
                }
                if (data.containsKey("is_active") && Boolean.FALSE.equals(data.get("is_active"))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This Jastiper is not available (INACTIVE).");
                }

                String extractedId = null;
                if (data.containsKey("id")) extractedId = data.get("id").toString();
                else if (data.containsKey("userId")) extractedId = data.get("userId").toString();
                else if (data.containsKey("user_id")) extractedId = data.get("user_id").toString();
                else if (data.containsKey("accountId")) extractedId = data.get("accountId").toString();

                if (extractedId != null) {
                    return UUID.fromString(extractedId);
                }
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid response format from Auth Module.");

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jastiper with username '" + username + "' not found.");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to Auth Module. Please ensure it is running.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getJastiperProfile(UUID jastiperId) {
        try {
            String url = authServiceUrl + "/profile/id/" + jastiperId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return (Map<String, Object>) response.get("data");
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}