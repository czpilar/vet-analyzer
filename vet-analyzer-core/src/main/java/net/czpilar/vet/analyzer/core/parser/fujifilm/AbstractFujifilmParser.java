package net.czpilar.vet.analyzer.core.parser.fujifilm;

import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmTestResult;
import net.czpilar.vet.analyzer.core.parser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFujifilmParser<T extends FujifilmMessage> implements MessageParser<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Splits raw message on comma delimiter. Preserves whitespace within fields
     * as Fujifilm uses fixed-width fields padded with spaces.
     */
    protected String[] splitFields(String data) {
        return data.split(",", -1);
    }

    protected String trimField(String field) {
        return field == null ? "" : field.trim();
    }

    protected LocalDate parseDate(String dateStr) {
        String trimmed = trimField(dateStr);
        if (trimmed.isEmpty()) return null;
        try {
            return LocalDate.parse(trimmed, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    protected LocalTime parseTime(String timeStr) {
        String trimmed = trimField(timeStr);
        if (trimmed.isEmpty()) return null;
        try {
            return LocalTime.parse(trimmed, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse time: {}", timeStr);
            return null;
        }
    }

    protected int parseIntSafe(String value) {
        String trimmed = trimField(value);
        if (trimmed.isEmpty()) return 0;
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Extracts the command character from the first field of raw data.
     */
    protected String extractCommand(String rawData) {
        if (rawData == null || rawData.isEmpty()) return "";
        int commaIndex = rawData.indexOf(',');
        String cmd = commaIndex > 0 ? rawData.substring(0, commaIndex) : rawData;
        return cmd.trim();
    }

    /**
     * Parses test results from fields array starting at given index.
     * Each test result consists of 7 fields:
     * testCode, relation, value+unit (combined field), dilutionFactor, rangeLow, rangeHigh, flag
     *
     * In real data the value and unit are in one fixed-width field, e.g. "74       g/l   "
     * which we split into value and unit parts.
     */
    protected List<FujifilmTestResult> parseTestResults(String[] fields, int startIndex, int numberOfTests) {
        List<FujifilmTestResult> results = new ArrayList<>();
        int fieldsPerTest = 7;

        for (int i = 0; i < numberOfTests; i++) {
            int base = startIndex + (i * fieldsPerTest);
            if (base + fieldsPerTest > fields.length) {
                break;
            }

            String testCode = trimField(fields[base]);
            String relation = trimField(fields[base + 1]);
            String valueUnit = fields[base + 2]; // combined value + unit, fixed width
            String[] vu = splitValueUnit(valueUnit);
            int dilutionFactor = parseIntSafe(fields[base + 3]);
            String rangeLow = trimField(fields[base + 4]);
            String rangeHigh = trimField(fields[base + 5]);
            String flag = trimField(fields[base + 6]);

            results.add(new FujifilmTestResult(
                    testCode, relation, vu[0], vu[1], dilutionFactor, rangeLow, rangeHigh, flag
            ));
        }

        return results;
    }

    /**
     * Splits a combined value+unit field like "74       g/l   " into ["74", "g/l"].
     * The value is numeric (possibly with decimal point) and the unit follows after whitespace.
     */
    private String[] splitValueUnit(String valueUnit) {
        String trimmed = valueUnit.trim();
        if (trimmed.isEmpty()) {
            return new String[]{"", ""};
        }

        // Find the boundary between value and unit
        // Value part: starts from beginning, may contain digits, dots, spaces (leading)
        // After the numeric part, there's whitespace then the unit
        int i = 0;
        // Skip leading space
        while (i < trimmed.length() && trimmed.charAt(i) == ' ') i++;

        int valueStart = i;
        // Consume value characters (digits, dot, minus, space within number)
        while (i < trimmed.length() && (Character.isDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '.' || trimmed.charAt(i) == '-')) {
            i++;
        }
        String value = trimmed.substring(valueStart, i).trim();

        // Skip whitespace between value and unit
        while (i < trimmed.length() && trimmed.charAt(i) == ' ') i++;

        String unit = i < trimmed.length() ? trimmed.substring(i).trim() : "";

        return new String[]{value, unit};
    }
}
