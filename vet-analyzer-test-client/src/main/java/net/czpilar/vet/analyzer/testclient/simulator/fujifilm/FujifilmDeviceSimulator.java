package net.czpilar.vet.analyzer.testclient.simulator.fujifilm;

import net.czpilar.vet.analyzer.core.protocol.fujifilm.StxEtxProtocol;
import net.czpilar.vet.analyzer.testclient.simulator.TcpDeviceSimulator;

import java.nio.charset.StandardCharsets;

public class FujifilmDeviceSimulator extends TcpDeviceSimulator {

    private final boolean useFraming;

    public FujifilmDeviceSimulator(String host, int port, boolean useFraming) {
        super(host, port);
        this.useFraming = useFraming;
    }

    public FujifilmDeviceSimulator(String host, int port) {
        this(host, port, true);
    }

    @Override
    protected byte[] encodeMessage(String message) {
        if (useFraming) {
            return StxEtxProtocol.frame(message.getBytes(StandardCharsets.ISO_8859_1));
        }
        return message.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Override
    protected String decodeResponse(byte[] data) {
        if (StxEtxProtocol.isFramed(data)) {
            return StxEtxProtocol.unframe(data);
        }
        return new String(data, StandardCharsets.ISO_8859_1);
    }
}
