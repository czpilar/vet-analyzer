package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.simulator.DeviceSimulator;
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
public class RawCommands extends AbstractDeviceCommands {

    public RawCommands(ConnectionState connectionState) {
        super(connectionState);
    }

    @Override
    protected String deviceName() { return "raw"; }

    @Override
    protected String displayName() { return "raw device"; }

    @Override
    protected DeviceSimulator createSimulator(String host, int port) {
        return new RawDeviceSimulator(host, port);
    }

    @Command(name = "raw connect", description = "Connect as unknown device (no protocol framing)")
    public String rawConnect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        return doConnect(host, port);
    }

    @Command(name = "raw send", description = "Send arbitrary text message")
    public String rawSend(
            @Option(longName = "message", defaultValue = "Hello from unknown device") String message) {
        return whenConnected(() -> {
            getSimulator().sendMessage(message);
            return "Sent raw message: " + message;
        });
    }

    @Command(name = "raw send binary", description = "Send binary data as hex string (e.g. '48454C4C4F')")
    public String rawSendBinary(
            @Option(longName = "hex") String hex) {
        return whenConnected(() -> {
            getSimulator().sendMessage(hex);
            return "Sent raw binary data (" + hex.length() / 2 + " bytes)";
        });
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
        return doDisconnect();
    }

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
