package  id.ac.ui.cs.advprog.jsoninventoryservice.dto.response;

import id.ac.ui.cs.advprog.jsoninventoryservice.model.ModerationLog;
import id.ac.ui.cs.advprog.jsoninventoryservice.model.enums.ModerationAction;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModerationLogResponseTest {
    @Test
    void testModerationLogResponse() {
        ModerationLog log = new ModerationLog();
        log.setLogId(UUID.randomUUID());
        log.setAction(ModerationAction.HIDE);

        ModerationLogResponse res = ModerationLogResponse.fromEntity(log);
        assertNotNull(res);
        assertEquals("HIDE", res.getAction());
    }

    @Test
    void testModerationLogResponse_NullAction() {
        ModerationLog log = new ModerationLog();
        log.setLogId(UUID.randomUUID());

        ModerationLogResponse res = ModerationLogResponse.fromEntity(log);
        assertNotNull(res);
        assertNull(res.getAction());
    }
}