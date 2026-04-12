package net.czpilar.vet.analyzer.core.parser.fujifilm.nx600;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmSampleInfoMessage;
import net.czpilar.vet.analyzer.core.parser.fujifilm.AbstractFujifilmParser;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;

/**
 * Parses NX600 W (sample info request) command.
 * Format: W,sampleNo
 */
public class Nx600SampleInfoParser extends AbstractFujifilmParser<FujifilmSampleInfoMessage> {

    @Override
    public boolean canParse(String rawData) {
        if (rawData == null) return false;
        return "W".equals(extractCommand(rawData));
    }

    @Override
    public FujifilmSampleInfoMessage parse(String rawData) {
        String[] fields = splitFields(rawData);
        String sampleNumber = fields.length > 1 ? trimField(fields[1]) : "";

        return new FujifilmSampleInfoMessage(
                AnalyzerType.NX600, FujifilmCommand.W,
                sampleNumber, rawData, Instant.now()
        );
    }
}
