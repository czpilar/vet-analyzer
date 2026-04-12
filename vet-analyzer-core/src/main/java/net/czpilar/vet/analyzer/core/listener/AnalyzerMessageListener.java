package net.czpilar.vet.analyzer.core.listener;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;

/**
 * Callback interface for receiving analyzer messages.
 * Implement this interface and register as a Spring bean to receive
 * parsed messages from the TCP server.
 */
public interface AnalyzerMessageListener {

    /**
     * Called when a message is received and successfully parsed.
     *
     * @param message       the parsed message
     * @param rawData       the raw message data as received
     * @param remoteAddress the IP address of the analyzer
     */
    void onMessage(AnalyzerMessage message, String rawData, String remoteAddress);

    /**
     * Called when a new TCP session starts (analyzer connects).
     */
    default void onSessionStart(String sessionId, String remoteAddress) {
    }

    /**
     * Called when a TCP session ends (analyzer disconnects).
     */
    default void onSessionEnd(String sessionId) {
    }

    /**
     * Called when a message is received but could not be parsed (unknown protocol).
     */
    default void onRawMessage(String rawData, String remoteAddress) {
    }
}
