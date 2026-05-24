package net.czpilar.vet.analyzer.server.web;

import net.czpilar.vet.analyzer.server.config.SessionProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/sessions")
public class SessionsController {

    private final Path sessionDirectory;
    private final ObjectMapper mapper;

    public SessionsController(SessionProperties properties, ObjectMapper mapper) {
        this.sessionDirectory = Path.of(properties.getDirectory());
        this.mapper = mapper;
    }

    @GetMapping
    public List<JsonNode> list() throws IOException {
        if (!Files.isDirectory(sessionDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(sessionDirectory)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::getFileName).reversed())
                    .map(this::readSummary)
                    .filter(node -> node != null)
                    .toList();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> detail(@PathVariable String id) {
        Path file = sessionDirectory.resolve(id + ".json");
        if (!isSafeChild(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.readTree(file.toFile()));
    }

    private JsonNode readSummary(Path file) {
        try {
            JsonNode node = mapper.readTree(file.toFile());
            return node.path("summary").isMissingNode() ? null : node.path("summary");
        } catch (JacksonException e) {
            return null;
        }
    }

    private boolean isSafeChild(Path file) {
        return file.normalize().startsWith(sessionDirectory.normalize());
    }
}
