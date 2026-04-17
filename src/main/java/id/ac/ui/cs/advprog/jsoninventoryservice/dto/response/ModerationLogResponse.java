package id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ModerationLogResponse {
    private UUID logId;
    private UUID adminId;
    private String action;
    private String reason;
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