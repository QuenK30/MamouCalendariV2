package fr.qmn.mamoucalendari.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeLib {
    //Get actual date at -> 2023-01-21
    public String getActualDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date());
    }

    //Get actual time at -> 12:00
    public String getActualTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(new Date());
    }

    //Get actual date at -> Monday 21 January
    public String getActualDateWithoutYear() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE dd MMMM");
        return formatter.format(new Date());
    }

    //Get actual date at -> 2023-01-21 and convert it to -> Monday 21 January
    public String getActualDateDay(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE dd MMMM");
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            return formatter.format(date1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
