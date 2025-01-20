package pl.bigxml.reader.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class HeaderFooter {
    private final StringBuilder header = new StringBuilder();
    private final StringBuilder footer = new StringBuilder();
    private final Map<String, Object> map = new HashMap<>();

    public <T> void putInMap(String key, T value) {
        map.put(key, value);
    }

    public <T> T getFromMap(String key, Class<T> type) {
        return type.cast(map.get(key));
    }
}
