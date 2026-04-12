# Vet Analyzer

Communication server for veterinary laboratory analyzers.

## Supported Analyzers

| Analyzer           | Manufacturer  | Type         | Protocol                           |
|--------------------|---------------|--------------|------------------------------------|
| BM850 / EXIGO H400 | Boule Medical | Hematology   | HL7 v2.7 over MLLP/TCP             |
| NX600 Series       | Fujifilm      | Biochemistry | Custom STX/ETX over TCP or RS-232C |
| AU20V              | Fujifilm      | Immunoassay  | Custom STX/ETX over TCP            |

## Project Structure

| Module                     | Description                                                                                                |
|----------------------------|------------------------------------------------------------------------------------------------------------|
| `vet-analyzer-core`        | Core library - protocol handling, message models, parsers. No Spring dependency, usable as standalone JAR. |
| `vet-analyzer-server`      | Spring Boot TCP server with Netty. Auto-detects analyzer type from incoming data. Logs sessions to files.  |
| `vet-analyzer-test-client` | Spring Shell interactive client simulating all 3 analyzer types.                                           |
| `vet-analyzer-app`         | Distribution package - produces ZIP with scripts, JARs, and configuration.                                 |

## Build

Requires Java 25.

```
./mvnw clean package
```

Produces ZIP distribution in `vet-analyzer-app/target/vet-analyzer-app-<version>.zip`.

## Running from Command Line

### Server

Via Maven wrapper:

```
./mvnw -pl :vet-analyzer-server spring-boot:run
```

Or via JAR:

```
java -jar vet-analyzer-server/target/vet-analyzer-server-<version>.jar
```

Or from ZIP distribution:

```
bin/vet-analyzer-server.bat
bin/vet-analyzer-server.sh
```

### Test Client

Via Maven wrapper:

```
./mvnw -pl :vet-analyzer-test-client spring-boot:run
```

Or via JAR:

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

Each TCP connection creates a session log file in the `sessions/` directory containing raw messages with parsed
annotations.

### Configuration

Edit `config/application.yml`:

```yaml
vet:
  analyzer:
    server:
      port: 9012
      session-directory: ./sessions
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

## Using Core Library in Other Projects

Add Maven dependency:

```xml

<dependency>
    <groupId>net.czpilar.vet.analyzer</groupId>
    <artifactId>vet-analyzer-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Parse messages:

```java
var registry = MessageParserRegistry.createDefault();
AnalyzerMessage message = registry.parse(rawData);

if (message instanceof FujifilmResultMessage result) {
    for (var test : result.testResults()) {
        System.out.println(test.testCode() + " = " + test.value() + " " + test.unit());
    }
}
```
