package pl.bigxml.reader.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PathConfigMaps {

    private final Map<String, MappingsConfig> configMap = new HashMap<>();
    private final Map<String, MappingsConfig> resultMap = new HashMap<>();

    public PathConfigMaps(List<MappingsConfig> mappingsConfigs) {
        for(MappingsConfig pc : mappingsConfigs) {
            configMap.put(pc.getXmlPath(), pc);
            resultMap.put(pc.getTargetName(), pc);
        }
    }

}
