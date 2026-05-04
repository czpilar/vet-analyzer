package net.czpilar.vet.analyzer.core.listener;

/**
 * Immutable snapshot of identifying information for a single TCP connection / session.
 * Passed to {@link AnalyzerMessageListener} callbacks so that listeners can correlate
 * events on a stable {@link #sessionId()} key (unique even for NAT'd peers and rapid
 * reconnects) and still display the {@link #remoteAddress()} when needed.
 *
 * @param sessionId     stable, unique identifier of the TCP session (e.g.
 *                      {@code yyyyMMdd-HHmmss-xxxx}); never {@code null}
 * @param remoteAddress IP address of the connected peer (e.g. {@code 192.168.1.10});
 *                      never {@code null}
 */
public record SessionContext(String sessionId, String remoteAddress) {
}
