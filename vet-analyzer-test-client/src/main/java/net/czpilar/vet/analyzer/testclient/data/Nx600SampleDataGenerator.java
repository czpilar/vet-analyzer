package net.czpilar.vet.analyzer.testclient.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Nx600SampleDataGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String generateResultMessage(String sampleNumber, String patientId) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);

        return "R,NORMAL ," + date + "," + time
                + "," + padRight(sampleNumber, 13)
                + "," + padRight(patientId, 13)
                + "," + padRight("", 13)
                + ",14,9,999,01,15"
                + ",TP-PS   ,=,62       g/l   ,01,55   ,75   ,           "
                + ",ALB-PS  ,=,36       g/l   ,01,26   ,40   ,           "
                + ",ALP-PS  ,=,0.67     ukat/l,01,0.10 ,4.00 ,           "
                + ",GLU-PS  ,=,5.7      mmol/l,01,3.1  ,6.7  ,           "
                + ",TBIL-PS ,=,5        umol/l,01,0    ,7    ,           "
                + ",IP-PS   ,=,1.82     mmol/l,01,1.00 ,2.10 ,           "
                + ",TCHO-PS ,=,7.81     mmol/l,01,3.50 ,7.80 ,H          "
                + ",GGT-PS  ,<,0.17     ukat/l,01,0.08 ,0.23 ,           "
                + ",GPT-PS  ,=,0.32     ukat/l,01,0.10 ,1.00 ,           "
                + ",Ca-PS   ,=,2.91     mmol/l,01,2.30 ,3.00 ,           "
                + ",CRE-PS  ,=,70       umol/l,01,35   ,110  ,           "
                + ",BUN-PS  ,=,7.36     mmol/l,01,3.30 ,8.30 ,           "
                + ",GLOB    ,=,26       g/l   ,01,16   ,37   ,           "
                + ",ALB/GLB ,=,1.4            ,01,0.7  ,1.9  ,           "
                + ",BUN/CRE ,=,105.1    (SI)  ,01,50.4 ,128.1,           ";
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
