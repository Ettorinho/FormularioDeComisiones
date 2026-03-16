package com.comisiones.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateFormatUtil {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private DateFormatUtil() {} // utilidad estática, no instanciar

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FMT) : "";
    }

    /**
     * Formatea un {@code java.util.Date} (o subtipo, como {@code java.sql.Date}
     * o {@code java.sql.Timestamp}) como "dd/MM/yyyy".
     * Retorna cadena vacía si el valor es null.
     */
    public static String formatUtilDate(Date date) {
        if (date == null) return "";
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DATE_FMT);
    }

    /**
     * Formatea un {@code java.sql.Timestamp} como "dd/MM/yyyy 'a las' HH:mm".
     * Retorna cadena vacía si el valor es null.
     */
    public static String formatTimestamp(java.sql.Timestamp ts) {
        if (ts == null) return "";
        return ts.toLocalDateTime().format(DATETIME_FMT);
    }
}
