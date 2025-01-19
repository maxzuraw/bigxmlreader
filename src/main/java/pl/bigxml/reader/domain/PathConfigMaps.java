package pl.bigxml.reader.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PathConfigMaps {

    private final Map<String, HeaderFooterConfig> configMap = new HashMap<>();
    private final Map<String, HeaderFooterConfig> resultMap = new HashMap<>();

    public PathConfigMaps(List<HeaderFooterConfig> headerFooterConfigs) {
        for(HeaderFooterConfig pc : headerFooterConfigs) {
            configMap.put(pc.getXmlPath(), pc);
            resultMap.put(pc.getTargetName(), pc);
        }
    }

}
