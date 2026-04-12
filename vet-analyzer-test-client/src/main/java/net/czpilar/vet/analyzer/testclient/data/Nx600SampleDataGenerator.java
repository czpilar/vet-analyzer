package net.czpilar.vet.analyzer.testclient.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Nx600SampleDataGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String generateResultMessage(String sampleNumber) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "R,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight("006532", 13)
                + "," + padRight("", 13)
                + ",16,9,255,01,3 "
                + ",TP-PS   ,=,68       g/l   ,1 ,55   ,75   ,           "
                + ",ALP-PS  ,=,2.15     ukat/l,1 ,0.10 ,4.00 ,           "
                + ",GLU-PS  ,=,5.8      mmol/l,1 ,3.1  ,6.7  ,           ";
    }

    public String generateStartMessage(String sampleNumber) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "S,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight("006532", 13)
                + "," + padRight("", 13)
                + ",01";
    }

    public String generateWorklistQuery(String sampleNumber, int count) {
        return "I," + padRight(sampleNumber, 13) + "," + count;
    }

    public String generateSampleInfoQuery(String sampleNumber) {
        return "W," + sampleNumber;
    }

    public String generateError() {
        return "E,001,Sample tray error";
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
