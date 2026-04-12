package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.data.Hl7SampleDataGenerator;
import net.czpilar.vet.analyzer.testclient.simulator.hl7.Hl7DeviceSimulator;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class Hl7Commands {

    private Hl7DeviceSimulator simulator;
    private final Hl7SampleDataGenerator dataGenerator = new Hl7SampleDataGenerator();

    @Command(name = "hl7 connect", description = "Connect as BM850/EXIGO H400 analyzer")
    public String hl7Connect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        try {
            simulator = new Hl7DeviceSimulator(host, port);
            simulator.connect();
            return "Connected to " + host + ":" + port + " as BM850/EXIGO H400";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }

    @Command(name = "hl7 send results", description = "Send hematology results")
    public String hl7SendResults(
            @Option(longName = "sampleId", defaultValue = "68") String sampleId) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'hl7 connect' first.";
        }
        try {
            String message = dataGenerator.generateResultMessage(sampleId);
            simulator.sendMessage(message);
            String ack = simulator.receiveResponse();
            return "Sent HL7 results for sample " + sampleId + ". ACK: " + (ack != null ? "received" : "none");
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "hl7 disconnect", description = "Disconnect BM850/EXIGO H400")
    public String hl7Disconnect() {
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
}
