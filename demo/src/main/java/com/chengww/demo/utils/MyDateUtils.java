package com.chengww.demo.utils;


import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by chengww on 2019/2/28.
 */
public class MyDateUtils {

    /**
     * Format UTC time as the relative time
     * @param utc UTC String
     * @return the relative time
     */
    public static String formateUTC(String utc) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(utc);
            return DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Date Error";
    }
}
