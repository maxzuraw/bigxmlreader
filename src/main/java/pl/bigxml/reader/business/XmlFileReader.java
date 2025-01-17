package pl.bigxml.reader.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.XmlReaderConfig;
import pl.bigxml.reader.domain.Appearance;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.domain.PathConfigMaps;
import pl.bigxml.reader.domain.PathTracker;
import pl.bigxml.reader.domain.PayInfo;
import pl.bigxml.reader.domain.Processing;
import pl.bigxml.reader.domain.ResultHolder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlFileReader {

    private final XmlReaderConfig xmlReaderConfig;

    public ResultHolder read(String pathToXmlFile, PathConfigMaps pathConfigMaps) throws FileNotFoundException, XMLStreamException {
        return processXmlForValues(pathToXmlFile, pathConfigMaps);
    }

    private ResultHolder processXmlForValues(String pathToXmlFile, PathConfigMaps pathConfigMaps) throws FileNotFoundException, XMLStreamException {
        Map<String, PathConfig> pathConfigPerPath = pathConfigMaps.getConfigMap();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, xmlReaderConfig.isNamespaceAware());
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        PathTracker pathTracker = new PathTracker();
        ResultHolder resultHolder = new ResultHolder();
        PayInfo payInfo = null;

        StringBuilder header = new StringBuilder();
        StringBuilder footer = new StringBuilder();
        StringBuilder currentSection = header;

        boolean insidePayInf = false;

        while(xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            PathConfig pathConfig = null;
            switch(event) {
                case XMLStreamConstants.START_ELEMENT:
                    String localName = xmlStreamReader.getLocalName();
                    if (xmlReaderConfig.getBodyNodeLocalName().equals(localName)
                            && xmlReaderConfig.getBodyNodeNamespaceUri().equals(xmlStreamReader.getNamespaceURI())) {
                        insidePayInf = true;
                        currentSection = footer;
                    }
                    if (!insidePayInf) {
                        appendStartElement(xmlStreamReader, currentSection);
                    }

                    pathTracker.addNextElement(localName);

                    pathConfig = pathConfigPerPath.get(pathTracker.getFullTrack());
                    if (isPayInfo(pathConfig)) {
                        payInfo = new PayInfo();
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    pathConfig = pathConfigPerPath.get(pathTracker.getFullTrack());
                    if (pathConfig != null) {
                        if (Appearance.MAP.equals(pathConfig.getAppearance())) {
                            resultHolder.putInMap(pathConfig.getTargetName(), xmlStreamReader.getText().trim());
                        }
                    }
                    if (!insidePayInf) {
                        currentSection.append(xmlStreamReader.getText());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    pathConfig = pathConfigPerPath.get(pathTracker.getFullTrack());
                    String lastElement = pathTracker.getLastElement();
                    if (lastElement.equals(xmlStreamReader.getLocalName())) {
                        log.debug(pathTracker.getFullTrack());
                        pathTracker.removeLastElement();
                    }
                    if (isPayInfo(pathConfig)) {
                        resultHolder.addToList(payInfo);
                    }

                    if (xmlReaderConfig.getBodyNodeLocalName().equals(xmlStreamReader.getLocalName())
                            && xmlReaderConfig.getBodyNodeNamespaceUri().equals(xmlStreamReader.getNamespaceURI())) {
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
        resultHolder.getHeader().append(header);
        String cleanedUpFooter = cleanUpFooter(footer);
        resultHolder.getFooter().append(cleanedUpFooter);
        return resultHolder;
    }

    private static boolean isPayInfo(PathConfig pathConfig) {
        return pathConfig != null &&
                pathConfig.getFullQualifiedClassName().equals(PayInfo.class.getCanonicalName()) &&
                pathConfig.getProcessing().equals(Processing.INCLUDE) &&
                pathConfig.getAppearance().equals(Appearance.LIST);
    }

    private static void appendStartElement(XMLStreamReader reader, StringBuilder builder) {
        builder.append("<");
        if (reader.getPrefix() != null && !reader.getPrefix().isEmpty()) {
            builder.append(reader.getPrefix()).append(":");
        }
        builder.append(reader.getLocalName());

        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            builder.append(" xmlns");
            if (reader.getNamespacePrefix(i) != null) {
                builder.append(":").append(reader.getNamespacePrefix(i));
            }
            builder.append("=\"").append(reader.getNamespaceURI(i)).append("\"");
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            builder.append(" ")
                    .append(reader.getAttributePrefix(i) != null && !reader.getAttributePrefix(i).isEmpty()
                            ? reader.getAttributePrefix(i) + ":"
                            : "")
                    .append(reader.getAttributeLocalName(i))
                    .append("=\"")
                    .append(reader.getAttributeValue(i))
                    .append("\"");
        }

        builder.append(">");
    }

    private static String cleanUpFooter(StringBuilder footer) {
        String footerString = footer.toString();
        return footerString.lines()
                .filter(line -> !line.trim().isEmpty())
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .orElse("");
    }

}
