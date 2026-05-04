package net.czpilar.vet.analyzer.core.listener;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;

/**
 * Callback interface for receiving analyzer messages.
 * Implement this interface and register as a Spring bean to receive
 * parsed messages from the TCP server.
 * <p>
 * All per-session callbacks ({@link #onSessionStart(SessionContext)},
 * {@link #onMessage(AnalyzerMessage, String, SessionContext)},
 * {@link #onRawMessage(String, SessionContext)}) carry a {@link SessionContext} so
 * implementations can correlate events on the stable {@code sessionId} key.
 * Only {@link #onSessionEnd(String)} keeps just the {@code sessionId} since the
 * underlying connection is already gone by then.
 */
public interface AnalyzerMessageListener {

    /**
     * Called when a message is received and successfully parsed.
     *
     * @param message the parsed message
     * @param rawData the raw message data as received
     * @param session session context (stable id + remote address)
     */
    void onMessage(AnalyzerMessage message, String rawData, SessionContext session);

    /**
     * Called when a new TCP session starts (analyzer connects).
     *
     * @param session session context (stable id + remote address)
     */
    default void onSessionStart(SessionContext session) {
    }

    /**
     * Called when a TCP session ends (analyzer disconnects). The remote address
     * is no longer meaningful at this point - only the session id is provided.
     *
     * @param sessionId stable id of the session that just ended
     */
    default void onSessionEnd(String sessionId) {
    }

    /**
     * Called when a message is received but could not be parsed (unknown protocol).
     *
     * @param rawData raw message data as received
     * @param session session context (stable id + remote address)
     */
    default void onRawMessage(String rawData, SessionContext session) {
    }
}
