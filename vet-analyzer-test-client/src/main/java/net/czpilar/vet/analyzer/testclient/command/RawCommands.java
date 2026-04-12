package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.simulator.TcpDeviceSimulator;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Commands for sending raw/unknown data to the server.
 * Useful for testing how the server handles unrecognized protocols.
 */
@Component
public class RawCommands {

    private TcpDeviceSimulator simulator;

    @Command(name = "raw connect", description = "Connect as unknown device (no protocol framing)")
    public String rawConnect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        try {
            simulator = new RawDeviceSimulator(host, port);
            simulator.connect();
            return "Connected to " + host + ":" + port + " as raw/unknown device";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }

    @Command(name = "raw send", description = "Send arbitrary text message")
    public String rawSend(
            @Option(longName = "message", defaultValue = "Hello from unknown device") String message) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'raw connect' first.";
        }
        try {
            simulator.sendMessage(message);
            return "Sent raw message: " + message;
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "raw send binary", description = "Send binary data as hex string (e.g. '48454C4C4F')")
    public String rawSendBinary(
            @Option(longName = "hex") String hex) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'raw connect' first.";
        }
        try {
            simulator.sendMessage(hex);
            return "Sent raw binary data (" + hex.length() / 2 + " bytes)";
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "raw all", description = "Connect, send various unknown messages, disconnect")
    public String rawAll(
            CommandContext ctx,
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        var out = ctx.outputWriter();
        CommandUtils.printAndDelay(out, rawConnect(host, port));
        CommandUtils.printAndDelay(out, rawSend("Hello from unknown device"));
        CommandUtils.printAndDelay(out, rawSend("SOME_PROPRIETARY_PROTOCOL:DATA1:DATA2:DATA3"));
        CommandUtils.printAndDelay(out, rawSend("{\"type\":\"json\",\"value\":42}"));
        return rawDisconnect();
    }

    @Command(name = "raw disconnect", description = "Disconnect unknown device")
    public String rawDisconnect() {
        if (simulator == null) {
            return "Not connected.";
        }
        try {
            simulator.disconnect();
            simulator = null;
            return "Disconnected.";
        } catch (Exception e) {
            return "Disconnect failed: " + e.getMessage();
        }
    }

    /**
     * Simple raw TCP simulator - no protocol framing, just plain text.
     */
    private static class RawDeviceSimulator extends TcpDeviceSimulator {

        RawDeviceSimulator(String host, int port) {
            super(host, port);
        }

        @Override
        protected byte[] encodeMessage(String message) {
            return message.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected String decodeResponse(byte[] data) {
            return new String(data, StandardCharsets.UTF_8);
        }
    }
}
