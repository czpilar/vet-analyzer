package net.czpilar.vet.analyzer.core.parser.boule.bm850;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.parser.hl7.AbstractHl7MessageParser;

/**
 * Strict parser for Boule Medical BM850 / EXIGO H400 hematology analyzer.
 * Accepts only HL7 v2.7 messages where MSH-3 (sending application) identifies
 * the device as BM850 or EXIGO. Anything else is rejected and falls through to
 * raw/unknown handling.
 */
public class Bm850MessageParser extends AbstractHl7MessageParser {

    @Override
    protected AnalyzerType analyzerType() {
        return AnalyzerType.BM850_EXIGO;
    }

    @Override
    protected boolean matches(String sendingApplication, String hl7Version) {
        if (sendingApplication == null || hl7Version == null) {
            return false;
        }
        String app = sendingApplication.toUpperCase();
        boolean isBm850 = app.contains("BM850") || app.contains("EXIGO");
        boolean isVersion27 = hl7Version.trim().startsWith("2.7");
        return isBm850 && isVersion27;
    }
}
