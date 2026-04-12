package net.czpilar.vet.analyzer.testclient.simulator;

import java.io.IOException;

public interface DeviceSimulator {

    void connect() throws IOException;

    void disconnect() throws IOException;

    boolean isConnected();

    void sendMessage(String message) throws IOException;

    String receiveResponse() throws IOException;
}
