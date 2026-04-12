package net.czpilar.vet.analyzer.core.parser.fujifilm.nx600;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmWorklistQueryMessage;
import net.czpilar.vet.analyzer.core.parser.fujifilm.AbstractFujifilmParser;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;

/**
 * Parses NX600 I (worklist index query) command.
 * Format: I,sampleNo,numberOfRequests
 */
public class Nx600WorklistParser extends AbstractFujifilmParser<FujifilmWorklistQueryMessage> {

    @Override
    public boolean canParse(String rawData) {
        if (rawData == null) return false;
        return "I".equals(extractCommand(rawData));
    }

    @Override
    public FujifilmWorklistQueryMessage parse(String rawData) {
        String[] fields = splitFields(rawData);

        String sampleNumber = fields.length > 1 ? trimField(fields[1]) : "";
        int numberOfRequests = fields.length > 2 ? parseIntSafe(fields[2]) : 0;

        return new FujifilmWorklistQueryMessage(
                AnalyzerType.NX600, FujifilmCommand.I,
                sampleNumber, "", "", numberOfRequests,
                rawData, Instant.now()
        );
    }
}
