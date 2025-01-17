package pl.bigxml.reader.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultHolder {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HashMap<String, String> map = new HashMap<>();
    @Getter
    private final List<Object> list = new ArrayList<>();
    @Getter
    private final StringBuilder header = new StringBuilder();
    @Getter
    private final StringBuilder footer = new StringBuilder();


    public void addToList(Object object) {
        list.add(object);
    }

    public void putInMap(String key, String value) {
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapValueByKey(String key, Class<T> type) {
        String value = map.get(key);
        if (value == null) {
            return null;
        }
        if (type == LocalDate.class) {
            return (T) LocalDate.parse(value, DEFAULT_DATE_FORMATTER);
        }
        return type.cast(map.get(key));
    }
}
