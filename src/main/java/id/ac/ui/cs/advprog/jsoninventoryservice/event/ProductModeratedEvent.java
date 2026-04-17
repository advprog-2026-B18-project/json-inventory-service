package id.ac.ui.cs.advprog.jsoninventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductModeratedEvent {
    private final UUID productId;
    private final UUID adminId;
    private final String action;
    private final String reason;
    private final String productName;
    private final UUID jastiperId;
}