package net.czpilar.vet.analyzer.core.parser.fujifilm.au20v;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmWorklistQueryMessage;
import net.czpilar.vet.analyzer.core.parser.fujifilm.AbstractFujifilmParser;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;

/**
 * Parses AU20V X/Y (order index query) command.
 * Format: X,sampleNo,patientId,patientName,numberOfRequests
 * or: Y,sampleNo,patientId,patientName,numberOfRequests (with reference interval range)
 */
public class Au20vWorklistParser extends AbstractFujifilmParser<FujifilmWorklistQueryMessage> {

    @Override
    public boolean canParse(String rawData) {
        if (rawData == null) return false;
        String cmd = extractCommand(rawData);
        return "X".equals(cmd) || "Y".equals(cmd);
    }

    @Override
    public FujifilmWorklistQueryMessage parse(String rawData) {
        String[] fields = splitFields(rawData);

        String cmdStr = trimField(fields[0]);
        FujifilmCommand command = "Y".equals(cmdStr) ? FujifilmCommand.Y : FujifilmCommand.X;

        String sampleNumber = fields.length > 1 ? trimField(fields[1]) : "";
        String patientId = fields.length > 2 ? trimField(fields[2]) : "";
        String patientName = fields.length > 3 ? trimField(fields[3]) : "";
        int numberOfRequests = fields.length > 4 ? parseIntSafe(fields[4]) : 0;

        return new FujifilmWorklistQueryMessage(
                AnalyzerType.AU20V, command,
                sampleNumber, patientId, patientName, numberOfRequests,
                rawData, Instant.now()
        );
    }
}
