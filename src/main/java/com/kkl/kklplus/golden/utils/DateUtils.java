package com.kkl.kklplus.golden.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateUtils {


    /**
     * 字符串转换成日期
     * @param str yyyy-MM-dd HH:mm:ss
     * @return date yyyy-MM-dd HH:mm:ss
     */
    public static Date StringDateTimeToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    /**
     * 字符串转换成日期
     * @param str yyyy-MM-dd"
     * @return date yyyy-MM-dd"
     */
    public static Date StringDateToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取年
     * @param date
     * @return
     */
    public static Integer getYear(Date date){
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int year = ca.get(Calendar.YEAR);//年份数值
        return year;
    }

    /**
     * 获取月
     * @param date
     * @return
     */
    public static Integer getMonth(Date date){
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int month = ca.get(Calendar.MONTH)+1;//月份数值
        return month;
    }

}
