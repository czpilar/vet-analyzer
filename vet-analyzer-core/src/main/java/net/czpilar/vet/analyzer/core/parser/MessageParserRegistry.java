package net.czpilar.vet.analyzer.core.parser;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.parser.boule.bm850.Bm850MessageParser;
import net.czpilar.vet.analyzer.core.parser.fujifilm.au20v.Au20vErrorParser;
import net.czpilar.vet.analyzer.core.parser.fujifilm.au20v.Au20vResultParser;
import net.czpilar.vet.analyzer.core.parser.fujifilm.au20v.Au20vStartParser;
import net.czpilar.vet.analyzer.core.parser.fujifilm.au20v.Au20vWorklistParser;
import net.czpilar.vet.analyzer.core.parser.fujifilm.nx600.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MessageParserRegistry {

    private static final Logger log = LoggerFactory.getLogger(MessageParserRegistry.class);

    private final List<MessageParser<?>> parsers;

    public MessageParserRegistry(List<MessageParser<?>> parsers) {
        this.parsers = parsers;
    }

    public AnalyzerMessage parse(String rawData) {
        for (MessageParser<?> parser : parsers) {
            if (parser.canParse(rawData)) {
                log.debug("Parsing with {}", parser.getClass().getSimpleName());
                return parser.parse(rawData);
            }
        }
        log.warn("No parser found for message: {}", rawData.length() > 50 ? rawData.substring(0, 50) + "..." : rawData);
        return null;
    }

    /**
     * Creates a registry with all default parsers.
     * AU20V-specific parsers are checked first (T, X, Y commands),
     * then NX600 parsers (R, I, W, S, E), then device-specific HL7 parsers.
     */
    public static MessageParserRegistry createDefault() {
        List<MessageParser<?>> parsers = new ArrayList<>();

        // AU20V-specific parsers first (unique commands T, X, Y)
        parsers.add(new Au20vResultParser());
        parsers.add(new Au20vWorklistParser());
        parsers.add(new Au20vStartParser());
        parsers.add(new Au20vErrorParser());

        // NX600 parsers (R, I, W, S, E - also handles shared commands)
        parsers.add(new Nx600ResultParser());
        parsers.add(new Nx600WorklistParser());
        parsers.add(new Nx600SampleInfoParser());
        parsers.add(new Nx600StartParser());
        parsers.add(new Nx600ErrorParser());

        // HL7-based device parsers (strict per device + protocol version)
        parsers.add(new Bm850MessageParser());

        return new MessageParserRegistry(parsers);
    }
}
