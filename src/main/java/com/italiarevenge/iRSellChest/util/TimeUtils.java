/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.util;

import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static long getTime(String time) throws ParseException, NullPointerException {
        long dur = 0L;
        for (String word : time.split(" ")) {
            if (word.length() < 2) {
                throw new ParseException("Could not parse the given date object", 1);
            }
            String timeUnitString = "" + word.toCharArray()[word.length() - 1];
            TimeUnit timeUnit = Arrays.stream(new TimeUnit[]{TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS}).filter(t -> t.toString().toLowerCase().startsWith(timeUnitString)).findFirst().orElse(null);
            if (timeUnit == null) {
                throw new ParseException("Could not parse the given date object", 1);
            }
            try {
                dur += timeUnit.toMillis(Integer.parseInt(word.substring(0, word.length() - 1)));
            }
            catch (NumberFormatException e) {
                throw new ParseException("Could not parse the given date object", 1);
            }
        }
        return dur;
    }

    public static String getReadableTime(long time) {
        Duration dur = Duration.ofMillis(time);
        return dur.toDays() != 0L ? String.format("%dd %dh %dm %ds", dur.toDays(), dur.toHours() % 24L, dur.toMinutes() % 60L, dur.getSeconds() % 60L) : (dur.toHours() != 0L ? String.format("%dh %dm %ds", dur.toHours() % 24L, dur.toMinutes() % 60L, dur.getSeconds() % 60L) : (dur.toMinutes() != 0L ? String.format("%dm %ds", dur.toMinutes() % 60L, dur.getSeconds() % 60L) : String.format("%ds", dur.getSeconds() % 60L)));
    }
}

