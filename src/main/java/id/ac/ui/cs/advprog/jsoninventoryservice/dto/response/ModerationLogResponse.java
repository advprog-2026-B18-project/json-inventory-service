package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ModerationLogResponse {
    @JsonProperty("log_id")
    private UUID logId;
    @JsonProperty("admin_id")
    private UUID adminId;
    private String action;
    private String reason;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ModerationLogResponse fromEntity(ModerationLog log) {
        return ModerationLogResponse.builder()
                .logId(log.getLogId())
                .adminId(log.getAdminId())
                .action(log.getAction() != null ? log.getAction().name() : null)
                .reason(log.getReason())
                .createdAt(log.getCreatedAt())
                .build();
    }
}