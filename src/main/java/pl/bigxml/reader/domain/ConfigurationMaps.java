package pl.bigxml.reader.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ConfigurationMaps {

    private final Map<String, MappingsConfig> configurationPerXmlPath = new HashMap<>();
    private final Map<String, MappingsConfig> configurationPerTargetField = new HashMap<>();

    public ConfigurationMaps(List<MappingsConfig> mappingsConfigs) {
        for(MappingsConfig pc : mappingsConfigs) {
            configurationPerXmlPath.put(pc.getXmlPath(), pc);
            configurationPerTargetField.put(pc.getTargetName(), pc);
        }
    }

}
