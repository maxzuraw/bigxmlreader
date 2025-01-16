package pl.bigxml.reader.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultHolder {

    private final HashMap<String, String> map = new HashMap<>();
    private final List<Object> list = new ArrayList<>();
    @Getter
    private final StringBuilder header = new StringBuilder();
    @Getter
    private final StringBuilder footer = new StringBuilder();


    public void appendToHeader(String text) {
        header.append(text);
    }

    public void appendToFooter(String text) {
        footer.append(text);
    }

    public void addToList(Object object) {
        list.add(object);
    }

    public int getListSize() {
        return list.size();
    }

    public void putInMap(String key, String value) {
        map.put(key, value);
    }

    public <T> T getMapValueByKey(String key, Class<T> type) {
        if (type == LocalDate.class) {
            return (T) LocalDate.parse(map.get(key), DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return type.cast(map.get(key));
    }
}
