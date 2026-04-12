package net.czpilar.vet.analyzer.testclient.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Au20vSampleDataGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String generateResultMessage(String sampleNumber) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "T,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight("111", 13)
                + "," + padRight("", 13)
                + ",49,9,999,01,01"
                + ",v-PRG   ,<, 0.20    ng/mL ,01,0.00 ,0.00 ,  #        ";
    }

    public String generateOrderQuery(String sampleNumber, int count) {
        return "X," + padRight(sampleNumber, 13) + ",,," + count;
    }

    public String generateOrderQueryWithRefRange(String sampleNumber, int count) {
        return "Y," + padRight(sampleNumber, 13) + ",,," + count;
    }

    public String generateStartMessage(String sampleNumber) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "S,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight("111", 13)
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
