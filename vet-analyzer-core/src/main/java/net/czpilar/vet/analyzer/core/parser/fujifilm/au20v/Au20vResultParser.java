package net.czpilar.vet.analyzer.core.parser.fujifilm.au20v;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmTestResult;
import net.czpilar.vet.analyzer.core.parser.fujifilm.AbstractFujifilmParser;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;
import java.util.List;

/**
 * Parses AU20V T/R (measurement results) command.
 * Format: T,status,date,time,sampleNo,patientId,patientName,species,sex,age,samplePos,numTests,
 * testCode1,relation1,valueUnit1,dilution1,rangeLow1,rangeHigh1,flag1,...
 * <p>
 * AU20V uses T for results (and sometimes R). Max 5 tests per sample.
 */
public class Au20vResultParser extends AbstractFujifilmParser<FujifilmResultMessage> {

    @Override
    public boolean canParse(String rawData) {
        if (rawData == null) return false;
        String cmd = extractCommand(rawData);
        return "T".equals(cmd);
    }

    @Override
    public FujifilmResultMessage parse(String rawData) {
        String[] fields = splitFields(rawData);

        String cmdStr = trimField(fields[0]);
        FujifilmCommand command = "T".equals(cmdStr) ? FujifilmCommand.T : FujifilmCommand.R;

        String status = fields.length > 1 ? trimField(fields[1]) : "";
        var date = fields.length > 2 ? parseDate(fields[2]) : null;
        var time = fields.length > 3 ? parseTime(fields[3]) : null;
        String sampleNumber = fields.length > 4 ? trimField(fields[4]) : "";
        String patientId = fields.length > 5 ? trimField(fields[5]) : "";
        String patientName = fields.length > 6 ? trimField(fields[6]) : "";
        int speciesCode = fields.length > 7 ? parseIntSafe(fields[7]) : 0;
        int sex = fields.length > 8 ? parseIntSafe(fields[8]) : 9;
        int age = fields.length > 9 ? parseIntSafe(fields[9]) : 999;
        int samplePosition = fields.length > 10 ? parseIntSafe(fields[10]) : 0;
        int numberOfTests = fields.length > 11 ? parseIntSafe(fields[11]) : 0;

        int testStartIndex = 12;
        List<FujifilmTestResult> testResults = parseTestResults(fields, testStartIndex, numberOfTests);

        return new FujifilmResultMessage(
                AnalyzerType.AU20V, command, status, date, time,
                sampleNumber, patientId, patientName, speciesCode, sex, age,
                samplePosition, numberOfTests, testResults, rawData, Instant.now()
        );
    }
}
