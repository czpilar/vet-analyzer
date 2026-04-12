package net.czpilar.vet.analyzer.testclient.command;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
public class ConnectionState {

    private static final String VET_ANALYZER = "vet:analyzer:";

    private final Set<String> connectedDevices = new TreeSet<>();

    public void addConnectedDevice(String device) {
        connectedDevices.add(device);
    }

    public void removeConnectedDevice(String device) {
        connectedDevices.remove(device);
    }

    public String getPrompt() {
        String prompt = VET_ANALYZER;
        if (!connectedDevices.isEmpty()) {
            prompt += String.join(",", connectedDevices);
        }
        return prompt + "> ";
    }
}
