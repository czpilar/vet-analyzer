package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.data.Au20vSampleDataGenerator;
import net.czpilar.vet.analyzer.testclient.simulator.DeviceSimulator;
import net.czpilar.vet.analyzer.testclient.simulator.fujifilm.FujifilmDeviceSimulator;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class Au20vCommands extends AbstractDeviceCommands {

    private final Au20vSampleDataGenerator dataGenerator = new Au20vSampleDataGenerator();

    public Au20vCommands(ConnectionState connectionState) {
        super(connectionState);
    }

    @Override
    protected String deviceName() { return "au20v"; }

    @Override
    protected String displayName() { return "AU20V"; }

    @Override
    protected DeviceSimulator createSimulator(String host, int port) {
        return new FujifilmDeviceSimulator(host, port);
    }

    @Command(name = "au20v connect", description = "Connect as Fujifilm AU20V analyzer")
    public String au20vConnect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        return doConnect(host, port);
    }

    @Command(name = "au20v send results", description = "Send immunoassay results")
    public String au20vSendResults(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateResultMessage(sampleNumber));
            return "Sent AU20V results for sample " + sampleNumber;
        });
    }

    @Command(name = "au20v send order query", description = "Send order query")
    public String au20vSendOrderQuery(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber,
            @Option(longName = "count", defaultValue = "5") Integer count) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateOrderQuery(sampleNumber, count));
            return "Sent AU20V order query";
        });
    }

    @Command(name = "au20v send order query ref range", description = "Send order query with reference interval range")
    public String au20vSendOrderQueryRefRange(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber,
            @Option(longName = "count", defaultValue = "5") Integer count) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateOrderQueryWithRefRange(sampleNumber, count));
            return "Sent AU20V order query with ref range";
        });
    }

    @Command(name = "au20v send error", description = "Send error notification")
    public String au20vSendError() {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateError());
            return "Sent AU20V error";
        });
    }

    @Command(name = "au20v full sequence", description = "Run full AU20V sequence (S -> T)")
    public String au20vFullSequence(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateStartMessage(sampleNumber));
            CommandUtils.delay(500);
            getSimulator().sendMessage(dataGenerator.generateResultMessage(sampleNumber));
            return "Sent full AU20V sequence (S + T) for sample " + sampleNumber;
        });
    }

    @Command(name = "au20v all", description = "Connect, send all message types, disconnect")
    public String au20vAll(
            CommandContext ctx,
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        var out = ctx.outputWriter();
        CommandUtils.printAndDelay(out, au20vConnect(host, port));
        CommandUtils.printAndDelay(out, au20vSendOrderQuery("1", 5));
        CommandUtils.printAndDelay(out, au20vSendOrderQueryRefRange("1", 5));
        CommandUtils.printAndDelay(out, au20vSendResults("1"));
        CommandUtils.printAndDelay(out, au20vSendError());
        CommandUtils.printAndDelay(out, au20vFullSequence("2"));
        return au20vDisconnect();
    }

    @Command(name = "au20v disconnect", description = "Disconnect AU20V")
    public String au20vDisconnect() {
        return doDisconnect();
    }
}
