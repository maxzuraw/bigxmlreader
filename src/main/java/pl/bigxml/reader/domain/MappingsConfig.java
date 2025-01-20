package pl.bigxml.reader.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MappingsConfig {
    private String xmlPath;
    private String classCanonicalName;
    private String targetName;
}
