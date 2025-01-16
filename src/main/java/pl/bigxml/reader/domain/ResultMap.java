package pl.bigxml.reader.domain;

import java.util.HashMap;

public class ResultMap {

    private final HashMap<String, String> map = new HashMap<>();

    public void put(String key, String value) {
        map.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(map.get(key));
    }
}
