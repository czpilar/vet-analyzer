# Vet Analyzer

Communication server for veterinary laboratory analyzers.

## Supported Analyzers

| Analyzer           | Manufacturer  | Type         | Protocol                           |
|--------------------|---------------|--------------|------------------------------------|
| BM850 / EXIGO H400 | Boule Medical | Hematology   | HL7 v2.7 over MLLP/TCP             |
| NX600 Series       | Fujifilm      | Biochemistry | Custom STX/ETX over TCP or RS-232C |
| AU20V              | Fujifilm      | Immunoassay  | Custom STX/ETX over TCP            |

## Project Structure

| Module                             | Description                                                                                       |
|------------------------------------|---------------------------------------------------------------------------------------------------|
| `vet-analyzer-core`                | Core library - protocol handling, message models, parsers, listener API. No Spring dependency.    |
| `vet-analyzer-spring-boot-starter` | Spring Boot Starter - auto-configures TCP server with Netty. Embeddable into any Spring Boot app. |
| `vet-analyzer-server`              | Standalone Spring Boot server - uses starter + file-based session logging.                        |
| `vet-analyzer-test-client`         | Spring Shell interactive client simulating all 3 analyzer types + raw/unknown device.             |
| `vet-analyzer-app`                 | Distribution package - produces ZIP with scripts, JARs, and configuration.                        |

## Build

Requires Java 25.

```
./mvnw clean package
```

Produces ZIP distribution in `vet-analyzer-app/target/vet-analyzer-app-<version>.zip`.

## Running from Command Line

First build the project:

```
./mvnw clean install -DskipTests
```

### Server

Via Maven wrapper (from project root):

```
./mvnw -pl :vet-analyzer-server spring-boot:run
```

Or via JAR (after build):

```
java -jar vet-analyzer-server/target/vet-analyzer-server-<version>.jar
```

Or from ZIP distribution:

```
bin/vet-analyzer-server.bat
bin/vet-analyzer-server.sh
```

### Test Client

Via Maven wrapper (from project root):

```
./mvnw -pl :vet-analyzer-test-client spring-boot:run
```

Or via JAR (after build):

```
java -jar vet-analyzer-test-client/target/vet-analyzer-test-client-<version>.jar
```

Or from ZIP distribution:

```
bin/vet-analyzer-test-client.bat
bin/vet-analyzer-test-client.sh
```

## Server

Server listens on a single TCP port (default 9012) and auto-detects the analyzer type from the first incoming message:
- **BM850/EXIGO** - detected by MLLP framing (0x0B) or `MSH|` prefix
- **NX600** - detected by STX framing + command `R`, `I`, `W`, `S`, `E`
- **AU20V** - detected by STX framing + command `T`, `X`, `Y`
- **Unknown** - any other protocol is logged as raw data

Each TCP connection creates a session log file in the `sessions/` directory containing raw messages with parsed annotations.

### Configuration

Edit `config/application.yml`:

```yaml
server:
  port: 8090

vet:
  analyzer:
    server:
      port: 9012
      session-directory: ./sessions
```

### REST API

The standalone server includes a REST API for managing the TCP analyzer server:

| Method | URL                    | Description                    |
|--------|------------------------|--------------------------------|
| GET    | `/api/analyzer/status` | Returns `{"running": true}`    |
| POST   | `/api/analyzer/start`  | Starts the TCP analyzer server |
| POST   | `/api/analyzer/stop`   | Stops the TCP analyzer server  |

Example:

```
curl http://localhost:8090/api/analyzer/status
curl -X POST http://localhost:8090/api/analyzer/start
curl -X POST http://localhost:8090/api/analyzer/stop
```

## Test Client Commands

Interactive shell with prompt `vet:analyzer>`.

### BM850/EXIGO H400 (HL7)

| Command            | Description                                       |
|--------------------|---------------------------------------------------|
| `hl7 connect`      | Connect to server                                 |
| `hl7 send results` | Send hematology results (RBC, WBC, HGB, PLT, ...) |
| `hl7 all`          | Connect, send all message types, disconnect       |
| `hl7 disconnect`   | Disconnect                                        |

### Fujifilm NX600 (Biochemistry)

| Command                  | Description                                             |
|--------------------------|---------------------------------------------------------|
| `nx600 connect`          | Connect to server                                       |
| `nx600 send results`     | Send biochemistry results (TP, ALP, GLU, GPT, CRE, ...) |
| `nx600 send start`       | Send test start notification                            |
| `nx600 send worklist`    | Send worklist query                                     |
| `nx600 send sample info` | Send sample info query                                  |
| `nx600 send error`       | Send error notification                                 |
| `nx600 full sequence`    | Run full S -> R sequence                                |
| `nx600 all`              | Connect, send all message types, disconnect             |
| `nx600 disconnect`       | Disconnect                                              |

### Fujifilm AU20V (Immunoassay)

| Command                            | Description                                        |
|------------------------------------|----------------------------------------------------|
| `au20v connect`                    | Connect to server                                  |
| `au20v send results`               | Send immunoassay results (v-PRG, v-TSH, v-T4, ...) |
| `au20v send order query`           | Send order query                                   |
| `au20v send order query ref range` | Send order query with reference interval range     |
| `au20v send error`                 | Send error notification                            |
| `au20v full sequence`              | Run full S -> T sequence                           |
| `au20v all`                        | Connect, send all message types, disconnect        |
| `au20v disconnect`                 | Disconnect                                         |

### Raw / Unknown Device

| Command           | Description                                        |
|-------------------|----------------------------------------------------|
| `raw connect`     | Connect as unknown device (no protocol framing)    |
| `raw send`        | Send arbitrary text message                        |
| `raw send binary` | Send binary data as hex string                     |
| `raw all`         | Connect, send various unknown messages, disconnect |
| `raw disconnect`  | Disconnect                                         |

All connect commands accept optional `--host` (default `localhost`) and `--port` (default `9012`) parameters.

## Session Log Format

```
=== SESSION START ===
Session ID: a1b2c3d4
Remote: 192.168.100.1
Started: 2025-09-15 10:30:00

Analyzer: Fujifilm NX600 (Biochemistry) [auto-detected]

--- MESSAGE [R - Measurement results] @ 2025-09-15 10:30:01 ---
R,NORMAL ,14-06-2019,09:28,8            ,006532       ,...
--- PARSED ---
Type: R (Measurement results)
Sample: 006532, Patient: , Species: 16
Tests: TP-PS=74 g/l [55-75], ALP-PS=4.51 ukat/l [0.10-4.00] H, ...
--- END MESSAGE ---

=== SESSION END === 2025-09-15 10:30:05
```

For unknown devices, messages are logged as `UNKNOWN` type without parsed section.

## Embedding in Existing Spring Boot Application

The `vet-analyzer-spring-boot-starter` module allows embedding the TCP analyzer server into any existing Spring Boot application (e.g. alongside Tomcat).

### 1. Add dependency

```xml
<dependency>
    <groupId>net.czpilar.vet.analyzer</groupId>
    <artifactId>vet-analyzer-spring-boot-starter</artifactId>
    <version>...</version>
</dependency>
```

### 2. Implement listener

```java
@Component
public class AnalyzerResultService implements AnalyzerMessageListener {

    @Override
    public void onMessage(AnalyzerMessage message, String rawData, String remoteAddress) {
        if (message instanceof FujifilmResultMessage result) {
            for (var test : result.testResults()) {
                // save to database, process, etc.
            }
        }
        if (message instanceof Hl7Message hl7) {
            for (var obs : hl7.observations()) {
                // save hematology results
            }
        }
    }

    @Override
    public void onSessionStart(String sessionId, String remoteAddress) {
        // optional: log new connection
    }

    @Override
    public void onSessionEnd(String sessionId) {
        // optional: cleanup
    }

    @Override
    public void onRawMessage(String rawData, String remoteAddress) {
        // optional: handle unknown protocol data
    }
}
```

### 3. Configure

```yaml
vet:
  analyzer:
    server:
      enabled: true
      auto-start: true
      port: 9012
```

| Property                                   | Default      | Description                                         |
|--------------------------------------------|--------------|-----------------------------------------------------|
| `vet.analyzer.server.enabled`              | `true`       | Enable/disable auto-configuration entirely          |
| `vet.analyzer.server.auto-start`           | `true`       | Start TCP server automatically on app startup       |
| `vet.analyzer.server.port`                 | `9012`       | TCP port to listen on                               |
| `vet.analyzer.server.idle-timeout-seconds` | `300`        | Close idle connections after N seconds (0=disabled) |
| `vet.analyzer.server.session-directory`    | `./sessions` | Directory for session log files                     |

The TCP server starts automatically alongside your application (e.g. Tomcat on port 8080 + analyzer server on port 9012). Multiple `AnalyzerMessageListener` beans can be registered - all will be notified.

### 4. Programmatic control (optional)

When `auto-start` is set to `false`, the server bean is created but not started. You can start/stop it programmatically - e.g. based on user action or database configuration:

```java
@Service
public class AnalyzerService {

    @Autowired
    private VetAnalyzerServerLifecycle analyzerServer;

    public void startAnalyzerServer() {
        if (!analyzerServer.isRunning()) {
            analyzerServer.start();
        }
    }

    public void stopAnalyzerServer() {
        if (analyzerServer.isRunning()) {
            analyzerServer.stop();
        }
    }

    public boolean isAnalyzerServerRunning() {
        return analyzerServer.isRunning();
    }
}
```

## Using Core Library Standalone

For projects that don't use Spring Boot, add the core library directly:

```xml
<dependency>
    <groupId>net.czpilar.vet.analyzer</groupId>
    <artifactId>vet-analyzer-core</artifactId>
    <version>...</version>
</dependency>
```

Parse messages:

```java
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmErrorMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmStartMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmWorklistQueryMessage;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;

public class Example {

    public static void main(String[] args) {
        String rawData = args[0];
        var registry = MessageParserRegistry.createDefault();
        AnalyzerMessage message = registry.parse(rawData);

        // BM850 / EXIGO H400 - HL7 hematology results
        if (message instanceof Hl7Message hl7) {
            System.out.println("HL7 " + hl7.messageType() + ", Sample: " + hl7.sampleId());
            for (var obs : hl7.observations()) {
                System.out.println(obs.observationId() + " = " + obs.value() + " " + obs.unit()
                        + " [" + obs.referenceRange() + "]");
            }
        }

        // Fujifilm NX600 / AU20V - biochemistry / immunoassay results
        if (message instanceof FujifilmResultMessage result) {
            System.out.println(result.analyzerType().displayName()
                    + ", Sample: " + result.sampleNumber()
                    + ", Patient: " + result.patientId());
            for (var test : result.testResults()) {
                System.out.println(test.testCode() + " " + test.relation() + " " + test.value()
                        + " " + test.unit() + " [" + test.rangeLow() + "-" + test.rangeHigh() + "]");
            }
        }

        // Fujifilm NX600 / AU20V - other message types
        if (message instanceof FujifilmStartMessage start) {
            System.out.println("Measurement started: " + start.sampleNumber());
        }
        if (message instanceof FujifilmErrorMessage error) {
            System.out.println("Error: " + error.errorData());
        }
        if (message instanceof FujifilmWorklistQueryMessage query) {
            System.out.println("Worklist query: " + query.sampleNumber());
        }
    }
}
```
