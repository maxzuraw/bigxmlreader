package pl.bigxml.reader.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CastingUtils {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String value, Class<T> type) {
        if (type.equals(LocalDate.class)) {
            return (T) LocalDate.parse(value, DEFAULT_DATE_FORMATTER);
        }
        if (type.equals(Integer.class)) {
            return (T) Integer.valueOf(value);
        }
        if (type.equals(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        }
        if (type.equals(BigDecimal.class)) {
            return (T) new BigDecimal(value);
        }
        return type.cast(value);
    }
}
