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

## Usage

### Server

```
bin/vet-analyzer-server.bat
```

Server listens on a single TCP port (default 9012) and auto-detects the analyzer type from the first incoming message:
- **BM850/EXIGO** - detected by MLLP framing (0x0B) or `MSH|` prefix
- **NX600** - detected by STX framing + command `R`, `I`, `W`, `S`, `E`
- **AU20V** - detected by STX framing + command `T`, `X`, `Y`

Each TCP connection creates a session log file in the `sessions/` directory containing raw messages with parsed annotations.

### Configuration

Edit `config/application.yml`:

```yaml
vet:
  analyzer:
    server:
      port: 9012
      session-directory: ./sessions
```

### Test Client

```
bin/vet-analyzer-test-client.bat
```

Available commands:

**BM850/EXIGO H400 (HL7):**
- `hl7-connect` - connect to server
- `hl7-send-results` - send hematology results (RBC, WBC, HGB, PLT, ...)
- `hl7-disconnect`

**Fujifilm NX600 (Biochemistry):**
- `nx600-connect` - connect to server
- `nx600-send-results` - send biochemistry results (TP, ALP, GLU, GPT, CRE, BUN, ...)
- `nx600-send-start` - send test start notification
- `nx600-send-worklist` - send worklist query
- `nx600-full-sequence` - run full S -> R sequence
- `nx600-disconnect`

**Fujifilm AU20V (Immunoassay):**
- `au20v-connect` - connect to server
- `au20v-send-results` - send immunoassay results (v-PRG, v-TSH, v-T4, ...)
- `au20v-send-order-query` - send order query
- `au20v-full-sequence` - run full S -> T sequence
- `au20v-disconnect`

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
