package pl.bigxml.reader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bigxmlreader.xml-reader")
@Data
public class XmlReaderConfig {

    private boolean namespaceAware;
    private String bodyNodeLocalName;
    private String bodyNodeNamespaceUri;

}
