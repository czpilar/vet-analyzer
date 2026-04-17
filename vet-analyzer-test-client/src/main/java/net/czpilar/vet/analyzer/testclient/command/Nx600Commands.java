package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.data.Nx600SampleDataGenerator;
import net.czpilar.vet.analyzer.testclient.simulator.DeviceSimulator;
import net.czpilar.vet.analyzer.testclient.simulator.fujifilm.FujifilmDeviceSimulator;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class Nx600Commands extends AbstractDeviceCommands {

    private final Nx600SampleDataGenerator dataGenerator = new Nx600SampleDataGenerator();

    public Nx600Commands(ConnectionState connectionState) {
        super(connectionState);
    }

    @Override
    protected String deviceName() { return "nx600"; }

    @Override
    protected String displayName() { return "NX600"; }

    @Override
    protected DeviceSimulator createSimulator(String host, int port) {
        return new FujifilmDeviceSimulator(host, port);
    }

    @Command(name = "nx600 connect", description = "Connect as Fujifilm NX600 analyzer")
    public String nx600Connect(
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        return doConnect(host, port);
    }

    @Command(name = "nx600 send results", description = "Send biochemistry results")
    public String nx600SendResults(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateResultMessage(sampleNumber));
            return "Sent NX600 results for sample " + sampleNumber;
        });
    }

    @Command(name = "nx600 send start", description = "Send test start notification")
    public String nx600SendStart(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateStartMessage(sampleNumber));
            return "Sent NX600 start for sample " + sampleNumber;
        });
    }

    @Command(name = "nx600 send sample info", description = "Send sample info query")
    public String nx600SendSampleInfo(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateSampleInfoQuery(sampleNumber));
            return "Sent NX600 sample info query for sample " + sampleNumber;
        });
    }

    @Command(name = "nx600 send worklist", description = "Send worklist query")
    public String nx600SendWorklistQuery(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber,
            @Option(longName = "count", defaultValue = "3") Integer count) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateWorklistQuery(sampleNumber, count));
            return "Sent NX600 worklist query";
        });
    }

    @Command(name = "nx600 full sequence", description = "Run full NX600 bidirectional sequence (S -> R)")
    public String nx600FullSequence(
            @Option(longName = "sampleNumber", defaultValue = "1") String sampleNumber) {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateStartMessage(sampleNumber));
            CommandUtils.delay(500);
            getSimulator().sendMessage(dataGenerator.generateResultMessage(sampleNumber));
            return "Sent full NX600 sequence (S + R) for sample " + sampleNumber;
        });
    }

    @Command(name = "nx600 send error", description = "Send error notification")
    public String nx600SendError() {
        return whenConnected(() -> {
            getSimulator().sendMessage(dataGenerator.generateError());
            return "Sent NX600 error";
        });
    }

    @Command(name = "nx600 all", description = "Connect, send all message types, disconnect")
    public String nx600All(
            CommandContext ctx,
            @Option(longName = "host", defaultValue = "localhost") String host,
            @Option(longName = "port", defaultValue = "9012") Integer port) {
        PrintWriter out = ctx.outputWriter();
        CommandUtils.printAndDelay(out, nx600Connect(host, port));
        CommandUtils.printAndDelay(out, nx600SendWorklistQuery("1", 3));
        CommandUtils.printAndDelay(out, nx600SendSampleInfo("1"));
        CommandUtils.printAndDelay(out, nx600SendStart("1"));
        CommandUtils.printAndDelay(out, nx600SendResults("1"));
        CommandUtils.printAndDelay(out, nx600SendError());
        return nx600Disconnect();
    }

    @Command(name = "nx600 disconnect", description = "Disconnect NX600")
    public String nx600Disconnect() {
        return doDisconnect();
    }
}
