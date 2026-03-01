package me.adaptiveservercore.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private TimeUtil() {
    }

    public static LocalTime parseHm(String value, LocalTime fallback) {
        try {
            return LocalTime.parse(value, TIME_FORMATTER);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static long secondsUntilNext(LocalTime target, ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next = now.withHour(target.getHour()).withMinute(target.getMinute()).withSecond(0).withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return Duration.between(now, next).getSeconds();
    }
}
