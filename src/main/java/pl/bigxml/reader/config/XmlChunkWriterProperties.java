package pl.bigxml.reader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="bigxmlreader.xml-chunk-writer")
@Data
public class XmlChunkWriterProperties {
    private String targetFolder;
    private String targetFilePrefix;
}
