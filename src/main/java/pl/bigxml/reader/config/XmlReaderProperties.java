package pl.bigxml.reader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bigxmlreader.xml-reader")
@Data
public class XmlReaderProperties {

    private boolean namespaceAware;
    private int chunkSize;
    private String bodyNodeLocalName;
    private String bodyNodeNamespaceUri;
    private String rootNodeLocalName;

}
