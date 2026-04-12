package net.czpilar.vet.analyzer.core.parser.fujifilm.nx600;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmErrorMessage;
import net.czpilar.vet.analyzer.core.parser.fujifilm.AbstractFujifilmParser;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;

/**
 * Parses NX600 E (error information) command.
 */
public class Nx600ErrorParser extends AbstractFujifilmParser<FujifilmErrorMessage> {

    @Override
    public boolean canParse(String rawData) {
        if (rawData == null) return false;
        return "E".equals(extractCommand(rawData));
    }

    @Override
    public FujifilmErrorMessage parse(String rawData) {
        String[] fields = splitFields(rawData);
        String errorData = fields.length > 1 ? rawData.substring(rawData.indexOf(',') + 1) : "";

        return new FujifilmErrorMessage(
                AnalyzerType.NX600, FujifilmCommand.E,
                errorData, rawData, Instant.now()
        );
    }
}
