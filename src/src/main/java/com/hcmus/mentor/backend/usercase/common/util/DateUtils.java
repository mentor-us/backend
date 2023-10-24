package com.hcmus.mentor.backend.usercase.common.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class DateUtils {

  public static Date atEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }

  public static Date atStartOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  public static Date atStartOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }

  public static Date atEndOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    return calendar.getTime();
  }

  public static boolean isToday(Date date) {
    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate today = LocalDate.now();
    return localDate.isEqual(today);
  }

  public static long getMinutes(Duration duration) {
    long seconds = duration.getSeconds();
    return seconds / 60;
  }

  public static String parseDuration(Duration isoDuration) {
    long minutes = getMinutes(isoDuration);
    if (minutes % 60 > 0) {
      minutes += 60 - (minutes % 60);
    }
    //        long years = remainDays / 365;
    //        StringBuilder builder = new StringBuilder();
    //        if (years > 0) {
    //            builder.append(years).append(" năm ");
    //            remainDays -= years * 365;
    //        }
    //
    //        long months = remainDays / 30;
    //
    //        if (months > 0) {
    //            builder.append(months).append(" tháng ");
    //            remainDays -= months * 30;
    //        }
    //        if (remainDays > 0) {
    //            builder.append(remainDays).append(" ngày");
    //        }
    //
    //        return builder.toString().trim();
    List<String> result = convertMinuteToYearMonthDay(minutes);
    if (result.size() == 1) {
      return result.get(0);
    }
    return result.get(0) + ", " + result.get(1);
  }

  private static List<String> convertMinuteToYearMonthDay(long minutes) {
    Map<String, Integer> units = new LinkedHashMap<>();
    units.put("tháng", 24 * 60 * 30);
    units.put("ngày", 24 * 60);
    units.put("giờ", 60);

    List<String> result = new ArrayList<>();
    long value = minutes;

    for (String name : units.keySet()) {
      int element = units.get(name);
      long p = value / element;
      if (p >= 1) result.add(p + " " + name);
      value %= element;
    }

    return result;
  }

  public static String formatDate(Date date) {
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd/MM/yyyy");
    return formatter.format(date);
  }
}
