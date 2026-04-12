package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.data.Nx600SampleDataGenerator;
import net.czpilar.vet.analyzer.testclient.simulator.fujifilm.FujifilmDeviceSimulator;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

public class Nx600Commands {

    private FujifilmDeviceSimulator simulator;
    private final Nx600SampleDataGenerator dataGenerator = new Nx600SampleDataGenerator();

    @Command(name = "nx600-connect", description = "Connect as Fujifilm NX600 analyzer")
    public String connect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") int port) {
        try {
            simulator = new FujifilmDeviceSimulator(host, port);
            simulator.connect();
            return "Connected to " + host + ":" + port + " as NX600";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }

    @Command(name = "nx600-send-results", description = "Send biochemistry results")
    public String sendResults(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'nx600-connect' first.";
        }
        try {
            String message = dataGenerator.generateResultMessage(sampleNumber);
            simulator.sendMessage(message);
            return "Sent NX600 results for sample " + sampleNumber;
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "nx600-send-start", description = "Send test start notification")
    public String sendStart(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'nx600-connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateStartMessage(sampleNumber));
            return "Sent NX600 start for sample " + sampleNumber;
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "nx600-send-worklist", description = "Send worklist query")
    public String sendWorklistQuery(
            @Option(longName = "sampleNumber", defaultValue = "") String sampleNumber,
            @Option(longName = "count", defaultValue = "3") int count) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'nx600-connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateWorklistQuery(sampleNumber, count));
            return "Sent NX600 worklist query";
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "nx600-full-sequence", description = "Run full NX600 bidirectional sequence (S -> R)")
    public String fullSequence(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'nx600-connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateStartMessage(sampleNumber));
            Thread.sleep(500);
            simulator.sendMessage(dataGenerator.generateResultMessage(sampleNumber));
            return "Sent full NX600 sequence (S + R) for sample " + sampleNumber;
        } catch (Exception e) {
            return "Sequence failed: " + e.getMessage();
        }
    }

    @Command(name = "nx600-disconnect", description = "Disconnect NX600")
    public String disconnect() {
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
