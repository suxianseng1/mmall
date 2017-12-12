package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by Administrator on 2017/12/12.
 */
public class DateTimeUtil {
    public static final String FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static Date strToDate(String date,String formatPattern){
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern(formatPattern);
        DateTime dateTime = dateFormat.parseDateTime(date);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date,String formatPattern){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatPattern);
    }

    public static Date strToDate(String date){
        return strToDate(date,FORMAT_PATTERN);
    }

    public static String dateToStr(Date date){
        return dateToStr(date,FORMAT_PATTERN);
    }

}
