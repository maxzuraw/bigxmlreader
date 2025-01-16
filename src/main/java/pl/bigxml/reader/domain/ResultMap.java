package pl.bigxml.reader.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class ResultMap {

    private final HashMap<String, String> map = new HashMap<>();

    public void put(String key, String value) {
        map.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        if (type == LocalDate.class) {
            return (T) LocalDate.parse(map.get(key), DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return type.cast(map.get(key));
    }
}
