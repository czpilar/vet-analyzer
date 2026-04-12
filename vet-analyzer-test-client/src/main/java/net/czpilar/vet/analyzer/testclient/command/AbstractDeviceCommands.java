package net.czpilar.vet.analyzer.testclient.command;

import net.czpilar.vet.analyzer.testclient.simulator.DeviceSimulator;

public abstract class AbstractDeviceCommands {

    protected final ConnectionState connectionState;
    private DeviceSimulator simulator;

    protected AbstractDeviceCommands(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    protected abstract String deviceName();

    protected abstract String displayName();

    protected abstract DeviceSimulator createSimulator(String host, int port);

    protected String doConnect(String host, int port) {
        if (simulator != null && simulator.isConnected()) {
            return "Already connected as " + displayName() + ". Disconnect first.";
        }
        try {
            simulator = createSimulator(host, port);
            simulator.connect();
            connectionState.addConnectedDevice(deviceName());
            return "Connected to " + host + ":" + port + " as " + displayName();
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }

    protected String doDisconnect() {
        if (simulator == null) {
            return "Not connected.";
        }
        try {
            simulator.disconnect();
            simulator = null;
            connectionState.removeConnectedDevice(deviceName());
            return "Disconnected " + displayName() + ".";
        } catch (Exception e) {
            return "Disconnect failed: " + e.getMessage();
        }
    }

    protected String whenConnected(ConnectedAction action) {
        if (simulator == null || !simulator.isConnected()) {
            return "Not connected. Use '" + deviceName() + " connect' first.";
        }
        try {
            return action.execute();
        } catch (Exception e) {
            return "Send failed: " + e.getMessage();
        }
    }

    protected DeviceSimulator getSimulator() {
        return simulator;
    }

    @FunctionalInterface
    protected interface ConnectedAction {
        String execute() throws Exception;
    }
}
