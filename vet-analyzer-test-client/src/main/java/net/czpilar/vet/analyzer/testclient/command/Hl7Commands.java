package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.data.Hl7SampleDataGenerator;
import net.czpilar.vet.analyzer.testclient.simulator.DeviceSimulator;
import net.czpilar.vet.analyzer.testclient.simulator.hl7.Hl7DeviceSimulator;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class Hl7Commands extends AbstractDeviceCommands {

    private final Hl7SampleDataGenerator dataGenerator = new Hl7SampleDataGenerator();

    public Hl7Commands(ConnectionState connectionState) {
        super(connectionState);
    }

    @Override
    protected String deviceName() { return "hl7"; }

    @Override
    protected String displayName() { return "BM850/EXIGO H400"; }

    @Override
    protected DeviceSimulator createSimulator(String host, int port) {
        return new Hl7DeviceSimulator(host, port);
    }

    @Command(name = "hl7 connect", description = "Connect as BM850/EXIGO H400 analyzer")
    public String hl7Connect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        return doConnect(host, port);
    }

    @Command(name = "hl7 send results", description = "Send hematology results")
    public String hl7SendResults(
            @Option(longName = "sampleId", defaultValue = "68") String sampleId) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateResultMessage(sampleId));
            String ack;
            try {
                ack = getSimulator().receiveResponse();
            } catch (Exception e) {
                ack = null;
            }
            return "Sent HL7 results for sample " + sampleId + ". ACK: " + (ack != null ? "received" : "not received");
        });
    }

    @Command(name = "hl7 all", description = "Connect, send all message types, disconnect")
    public String hl7All(
            CommandContext ctx,
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        PrintWriter out = ctx.outputWriter();
        CommandUtils.printAndDelay(out, hl7Connect(host, port));
        CommandUtils.printAndDelay(out, hl7SendResults("68"));
        CommandUtils.printAndDelay(out, hl7SendResults("69"));
        return hl7Disconnect();
    }

    @Command(name = "hl7 disconnect", description = "Disconnect BM850/EXIGO H400")
    public String hl7Disconnect() {
        return doDisconnect();
    }
}
