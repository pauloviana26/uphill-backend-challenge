package uphill.backend.challenge.model;

import java.io.PrintWriter;
import java.util.UUID;

public class Session {
    private final UUID sessionId;
    private final PrintWriter out;
    private String name;
    private long startTime;

    public Session(UUID sessionId, PrintWriter out, long startTime) {
        this.sessionId = sessionId;
        this.out = out;
        this.startTime = startTime;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public void send(String message) {
        out.println(message);
    }
}
