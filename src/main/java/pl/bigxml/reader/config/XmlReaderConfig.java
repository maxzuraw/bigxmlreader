package pl.bigxml.reader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "bigxmlreader.xml-reader")
@Data
public class XmlReaderConfig {

    private boolean namespaceAware;
    private boolean removeNamespaceManuallyEnabled;
    private String namespacesCommaSeparated;

    public List<String> getNamespaces() {
        String[] split = namespacesCommaSeparated.split(",");
        List<String> namespaces = new ArrayList<>();
        for(String s : split) {
            namespaces.add(s + ":");
        }
        return namespaces;
    }
}
