package com.example.sanket.dateutils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sanket on 10/16/15.
 */
public class DateUtil {

    public static String convertDate(String fromDate) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date convertedDate = null;

        try {

            convertedDate = sdf.parse(fromDate);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
            String convertedDateString = formatter.format(convertedDate);

            return convertedDateString;
        } catch(Exception ex){

            ex.printStackTrace();
            return null;
        }
    }
}