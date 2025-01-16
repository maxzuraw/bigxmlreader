package pl.bigxml.reader.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PathConfig {
    private String xmlPath;
    private String fullQualifiedClassName;
    private String targetName;
}
