package net.czpilar.vet.analyzer.testclient.simulator.hl7;

import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;
import net.czpilar.vet.analyzer.testclient.simulator.TcpDeviceSimulator;

public class Hl7DeviceSimulator extends TcpDeviceSimulator {

    public Hl7DeviceSimulator(String host, int port) {
        super(host, port);
    }

    @Override
    protected byte[] encodeMessage(String message) {
        return Hl7Protocol.wrapMllp(message);
    }

    @Override
    protected String decodeResponse(byte[] data) {
        return Hl7Protocol.unwrapMllp(data);
    }
}
