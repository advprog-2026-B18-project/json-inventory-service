package id.ac.ui.cs.advprog.jsoninventoryservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductUpdateRequest {
    private String action;
    private String reason;
}