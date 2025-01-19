package pl.bigxml.reader.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayinfoMappingConfig {
    private String xmlPath;
    private String targetPropertyName;
}
