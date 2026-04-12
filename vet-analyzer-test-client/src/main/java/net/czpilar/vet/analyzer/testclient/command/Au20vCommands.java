package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.data.Au20vSampleDataGenerator;
import net.czpilar.vet.analyzer.testclient.simulator.fujifilm.FujifilmDeviceSimulator;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class Au20vCommands {

    private FujifilmDeviceSimulator simulator;
    private final Au20vSampleDataGenerator dataGenerator = new Au20vSampleDataGenerator();

    @Command(name = "au20v connect", description = "Connect as Fujifilm AU20V analyzer")
    public String au20vConnect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        try {
            simulator = new FujifilmDeviceSimulator(host, port);
            simulator.connect();
            return "Connected to " + host + ":" + port + " as AU20V";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }

    @Command(name = "au20v send results", description = "Send immunoassay results")
    public String au20vSendResults(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'au20v connect' first.";
        }
        try {
            String message = dataGenerator.generateResultMessage(sampleNumber);
            simulator.sendMessage(message);
            return "Sent AU20V results for sample " + sampleNumber;
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "au20v send order query", description = "Send order query")
    public String au20vSendOrderQuery(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber,
            @Option(longName = "count", defaultValue = "5") Integer count) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'au20v connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateOrderQuery(sampleNumber, count));
            return "Sent AU20V order query";
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "au20v send order query ref range", description = "Send order query with reference interval range")
    public String au20vSendOrderQueryRefRange(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber,
            @Option(longName = "count", defaultValue = "5") Integer count) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'au20v connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateOrderQueryWithRefRange(sampleNumber, count));
            return "Sent AU20V order query with ref range";
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "au20v send error", description = "Send error notification")
    public String au20vSendError() {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'au20v connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateError());
            return "Sent AU20V error";
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    @Command(name = "au20v full sequence", description = "Run full AU20V sequence (S -> T)")
    public String au20vFullSequence(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use 'au20v connect' first.";
        }
        try {
            simulator.sendMessage(dataGenerator.generateStartMessage(sampleNumber));
            Thread.sleep(500);
            simulator.sendMessage(dataGenerator.generateResultMessage(sampleNumber));
            return "Sent full AU20V sequence (S + T) for sample " + sampleNumber;
        } catch (Exception e) {
            return "Sequence failed: " + e.getMessage();
        }
    }

    @Command(name = "au20v all", description = "Connect, send all message types, disconnect")
    public String au20vAll(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        var sb = new StringBuilder();
        sb.append(au20vConnect(host, port)).append(CommandUtils.NL);
        CommandUtils.delay();
        sb.append(au20vSendOrderQuery("1", 5)).append(CommandUtils.NL);
        CommandUtils.delay();
        sb.append(au20vSendOrderQueryRefRange("1", 5)).append(CommandUtils.NL);
        CommandUtils.delay();
        sb.append(au20vSendResults("1")).append(CommandUtils.NL);
        CommandUtils.delay();
        sb.append(au20vSendError()).append(CommandUtils.NL);
        CommandUtils.delay();
        sb.append(au20vFullSequence("2")).append(CommandUtils.NL);
        CommandUtils.delay();
        sb.append(au20vDisconnect());
        return sb.toString();
    }

    @Command(name = "au20v disconnect", description = "Disconnect AU20V")
    public String au20vDisconnect() {
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
