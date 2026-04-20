package net.czpilar.vet.analyzer.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vet.analyzer.session")
public class SessionProperties {

    private String directory = "./sessions";

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
