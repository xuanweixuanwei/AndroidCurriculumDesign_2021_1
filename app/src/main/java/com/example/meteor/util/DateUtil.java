package com.example.meteor.util;

import java.util.Calendar;

public class DateUtil {
    public static String MillisToStr(long timeMillis){
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timeMillis);
        return instance.get(Calendar.YEAR)+"年"+instance.get(Calendar.MONTH)+"月"+instance.get(Calendar.DAY_OF_MONTH)+"日"
                +instance.get(Calendar.HOUR)+"时"+instance.get(Calendar.MINUTE)+"分";
    }
}
