package pl.bigxml.reader.business.headerandfooter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.business.MappingsFileReader;
import pl.bigxml.reader.config.XmlReaderProperties;
import pl.bigxml.reader.domain.ConfigurationMaps;
import pl.bigxml.reader.domain.HeaderFooter;
import pl.bigxml.reader.domain.MappingsConfig;
import pl.bigxml.reader.domain.PathTracker;
import pl.bigxml.reader.utils.CastingUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import static pl.bigxml.reader.business.ElementReader.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderFooterProcessor {

    private final XmlReaderProperties xmlReaderProperties;
    private final MappingsFileReader mappingsFileReader;

    public HeaderFooter read(String pathToXmlFile) throws FileNotFoundException, XMLStreamException, ClassNotFoundException {
        return readHeaderFooter(pathToXmlFile);
    }

    private HeaderFooter readHeaderFooter(String pathToXmlFile) throws FileNotFoundException, XMLStreamException, ClassNotFoundException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, xmlReaderProperties.isNamespaceAware());
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        var mappingsConfigs = mappingsFileReader.readHeaderFooterMappings();
        ConfigurationMaps maps = new ConfigurationMaps(mappingsConfigs);
        Map<String, MappingsConfig> configurationPerXmlPath = maps.getConfigurationPerXmlPath();
        HeaderFooter headerFooter = new HeaderFooter();

        StringBuilder header = new StringBuilder();
        StringBuilder footer = new StringBuilder();
        StringBuilder currentSection = header;
        boolean insidePayInf = false;
        PathTracker pathTracker = new PathTracker();
        while(xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch(event) {
                case XMLStreamConstants.START_ELEMENT:
                    String localName = xmlStreamReader.getLocalName();
                    if (isElementBodyNode(xmlStreamReader)) {
                        insidePayInf = true;
                        currentSection = footer;
                    }
                    if (!insidePayInf) {
                        currentSection.append("<");
                        appendPrefixIfExists(xmlStreamReader, currentSection);
                        currentSection.append(xmlStreamReader.getLocalName());
                        appendNamespacesIfExists(xmlStreamReader, currentSection);
                        appendAttributesIfExists(xmlStreamReader, currentSection);
                        currentSection.append(">");
                    }
                    pathTracker.addNextElement(localName);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (!insidePayInf) {
                        currentSection.append(xmlStreamReader.getText());
                    }
                    var mappingsConfig = configurationPerXmlPath.get(pathTracker.getFullTrack());
                    if (mappingsConfig != null) {
                        Class<?> aClass = Class.forName(mappingsConfig.getClassCanonicalName());
                        headerFooter.putInMap(
                                mappingsConfig.getTargetName(),
                                CastingUtils.toObject(xmlStreamReader.getText(), aClass)
                        );
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    String lastElement = pathTracker.getLastElement();
                    if (lastElement.equals(xmlStreamReader.getLocalName())) {
                        pathTracker.removeLastElement();
                    }

                    if (isElementBodyNode(xmlStreamReader)) {
                        insidePayInf = false;
                    } else if (!insidePayInf) {
                        currentSection.append("</").append(xmlStreamReader.getPrefix()).append(":")
                                .append(xmlStreamReader.getLocalName()).append(">");
                    }
                    break;
                case XMLStreamConstants.COMMENT:
                    if (!insidePayInf) {
                        currentSection.append("<!--").append(xmlStreamReader.getText()).append("-->");
                    }
                    break;
            }
        }
        headerFooter.getHeader().append(header);
        String cleanedUpFooter = cleanUpFooter(footer);
        headerFooter.getFooter().append(cleanedUpFooter);
        return headerFooter;
    }

    private boolean isElementBodyNode(XMLStreamReader xmlStreamReader) {
        return xmlReaderProperties.getBodyNodeLocalName().equals(xmlStreamReader.getLocalName())
                && xmlReaderProperties.getBodyNodeNamespaceUri().equals(xmlStreamReader.getNamespaceURI());
    }

    private static String cleanUpFooter(StringBuilder footer) {
        String footerString = footer.toString();
        return footerString.lines()
                .filter(line -> !line.trim().isEmpty())
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .orElse("");
    }

}
