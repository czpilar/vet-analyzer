package net.czpilar.vet.analyzer.core.parser.fujifilm.nx600;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmStartMessage;
import net.czpilar.vet.analyzer.core.parser.fujifilm.AbstractFujifilmParser;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;

/**
 * Parses NX600 S (test start) command.
 * Format: S,testCondition,date,time,sampleNo,patientId,patientName,samplePosition
 */
public class Nx600StartParser extends AbstractFujifilmParser<FujifilmStartMessage> {

    @Override
    public boolean canParse(String rawData) {
        if (rawData == null) return false;
        return "S".equals(extractCommand(rawData));
    }

    @Override
    public FujifilmStartMessage parse(String rawData) {
        String[] fields = splitFields(rawData);

        String testCondition = fields.length > 1 ? trimField(fields[1]) : "";
        var date = fields.length > 2 ? parseDate(fields[2]) : null;
        var time = fields.length > 3 ? parseTime(fields[3]) : null;
        String sampleNumber = fields.length > 4 ? trimField(fields[4]) : "";
        String patientId = fields.length > 5 ? trimField(fields[5]) : "";
        String patientName = fields.length > 6 ? trimField(fields[6]) : "";
        int samplePosition = fields.length > 7 ? parseIntSafe(fields[7]) : 0;

        return new FujifilmStartMessage(
                AnalyzerType.NX600, FujifilmCommand.S,
                testCondition, date, time, sampleNumber, patientId, patientName,
                samplePosition, rawData, Instant.now()
        );
    }
}
