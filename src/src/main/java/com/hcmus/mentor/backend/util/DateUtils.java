package com.hcmus.mentor.backend.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DateUtils {

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

    public static String formatDate(LocalDateTime date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd/MM/yyyy");
        return formatter.format(date);
    }
}
