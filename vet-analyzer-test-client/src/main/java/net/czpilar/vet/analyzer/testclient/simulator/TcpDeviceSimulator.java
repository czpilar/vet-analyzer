package net.czpilar.vet.analyzer.testclient.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public abstract class TcpDeviceSimulator implements DeviceSimulator {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final String host;
    private final int port;

    protected TcpDeviceSimulator(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(10000);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        log.info("Connected to {}:{}", host, port);
    }

    @Override
    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            log.info("Disconnected from {}:{}", host, port);
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    @Override
    public void sendMessage(String message) throws IOException {
        byte[] data = encodeMessage(message);
        outputStream.write(data);
        outputStream.flush();
        log.info("Sent {} bytes", data.length);
    }

    @Override
    public String receiveResponse() throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead < 0) {
            return null;
        }
        byte[] data = new byte[bytesRead];
        System.arraycopy(buffer, 0, data, 0, bytesRead);
        return decodeResponse(data);
    }

    protected abstract byte[] encodeMessage(String message);

    protected abstract String decodeResponse(byte[] data);
}
