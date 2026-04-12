package net.czpilar.vet.analyzer.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vet.analyzer.server")
public class ServerProperties {

    private int port = 9012;
    private String sessionDirectory = "./sessions";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSessionDirectory() {
        return sessionDirectory;
    }

    public void setSessionDirectory(String sessionDirectory) {
        this.sessionDirectory = sessionDirectory;
    }
}
