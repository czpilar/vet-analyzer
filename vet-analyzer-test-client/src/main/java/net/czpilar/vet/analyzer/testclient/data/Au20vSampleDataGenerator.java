package net.czpilar.vet.analyzer.testclient.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Au20vSampleDataGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String generateResultMessage(String sampleNumber, String patientId) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "T,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight(patientId, 13)
                + "," + padRight("", 13)
                + ",14,9,999,01,01"
                + ",v-PRG   ,=, 3.34    ng/mL ,01,3.70 ,8.90 ,L          ,0";
    }

    public String generateOrderQuery(String sampleNumber, int count) {
        return "X," + padRight(sampleNumber, 13) + ",,," + count;
    }

    public String generateOrderQueryWithRefRange(String sampleNumber, int count) {
        return "Y," + padRight(sampleNumber, 13) + ",,," + count;
    }

    public String generateStartMessage(String sampleNumber, String patientId) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "S,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight(patientId, 13)
                + "," + padRight("", 13)
                + ",01";
    }

    public String generateError() {
        return "E,002,Measurement error";
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
