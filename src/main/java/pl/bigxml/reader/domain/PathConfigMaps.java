package pl.bigxml.reader.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PathConfigMaps {

    private final Map<String, PathConfig> configMap = new HashMap<>();
    private final Map<String, PathConfig> resultMap = new HashMap<>();

    public PathConfigMaps(List<PathConfig> pathConfigs) {
        for(PathConfig pc : pathConfigs) {
            configMap.put(pc.getXmlPath(), pc);
            resultMap.put(pc.getTargetName(), pc);
        }
    }

}
