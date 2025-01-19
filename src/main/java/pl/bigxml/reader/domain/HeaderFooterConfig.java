package pl.bigxml.reader.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeaderFooterConfig {
    private String xmlPath;
    private String fullQualifiedClassName;
    private String targetName;
    private Appearance appearance;
    private Processing processing;
}
