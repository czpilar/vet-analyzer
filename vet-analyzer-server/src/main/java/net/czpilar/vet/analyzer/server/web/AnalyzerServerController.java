package net.czpilar.vet.analyzer.server.web;

import net.czpilar.vet.analyzer.server.dto.AnalyzerServerStatus;
import net.czpilar.vet.analyzer.starter.config.VetAnalyzerServerLifecycle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyzer")
public class AnalyzerServerController {

    private final VetAnalyzerServerLifecycle serverLifecycle;

    public AnalyzerServerController(VetAnalyzerServerLifecycle serverLifecycle) {
        this.serverLifecycle = serverLifecycle;
    }

    @GetMapping("/status")
    public AnalyzerServerStatus status() {
        return AnalyzerServerStatus.of(serverLifecycle.isRunning());
    }

    @PostMapping("/start")
    public AnalyzerServerStatus start() {
        if (serverLifecycle.isRunning()) {
            return new AnalyzerServerStatus(true, "Already running");
        }
        serverLifecycle.start();
        return new AnalyzerServerStatus(true, "Started");
    }

    @PostMapping("/stop")
    public AnalyzerServerStatus stop() {
        if (!serverLifecycle.isRunning()) {
            return new AnalyzerServerStatus(false, "Already stopped");
        }
        serverLifecycle.stop();
        return new AnalyzerServerStatus(false, "Stopped");
    }

    @PostMapping("/restart")
    public AnalyzerServerStatus restart() {
        if (serverLifecycle.isRunning()) {
            serverLifecycle.stop();
        }
        serverLifecycle.start();
        return new AnalyzerServerStatus(true, "Restarted");
    }
}
