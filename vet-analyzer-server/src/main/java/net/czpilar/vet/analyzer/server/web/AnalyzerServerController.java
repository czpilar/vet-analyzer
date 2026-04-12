package net.czpilar.vet.analyzer.server.web;

import net.czpilar.vet.analyzer.starter.config.VetAnalyzerServerLifecycle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analyzer")
public class AnalyzerServerController {

    private final VetAnalyzerServerLifecycle serverLifecycle;

    public AnalyzerServerController(VetAnalyzerServerLifecycle serverLifecycle) {
        this.serverLifecycle = serverLifecycle;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("running", serverLifecycle.isRunning());
    }

    @PostMapping("/start")
    public Map<String, Object> start() {
        if (serverLifecycle.isRunning()) {
            return Map.of("running", true, "message", "Already running");
        }
        serverLifecycle.start();
        return Map.of("running", true, "message", "Started");
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        if (!serverLifecycle.isRunning()) {
            return Map.of("running", false, "message", "Already stopped");
        }
        serverLifecycle.stop();
        return Map.of("running", false, "message", "Stopped");
    }
}
