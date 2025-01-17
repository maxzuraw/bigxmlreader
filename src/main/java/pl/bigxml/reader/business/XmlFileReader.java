package pl.bigxml.reader.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.XmlReaderConfig;
import pl.bigxml.reader.domain.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlFileReader {

    private final XmlReaderConfig xmlReaderConfig;

    private static boolean isPayInfo(PathConfig pathConfig) {
        return pathConfig != null &&
                pathConfig.getFullQualifiedClassName().equals(PayInfo.class.getCanonicalName()) &&
                pathConfig.getProcessing().equals(Processing.INCLUDE) &&
                pathConfig.getAppearance().equals(Appearance.LIST);
    }

    public ResultHolder read(String pathToXmlFile, PathConfigMaps pathConfigMaps) throws FileNotFoundException, XMLStreamException {
        ResultHolder resultHolder = processXmlForValues(pathToXmlFile, pathConfigMaps);
        processXmlForHeaderAndFooter(pathToXmlFile, resultHolder);
        return resultHolder;
    }

    private ResultHolder processXmlForValues(String pathToXmlFile, PathConfigMaps pathConfigMaps) throws FileNotFoundException, XMLStreamException {
        Map<String, PathConfig> pathConfigPerPath = pathConfigMaps.getConfigMap();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        // NOTE: for some reason this property does not work on my environment
        // so if it does not work, there is manual processing of elements to omit namespaces
        // for that purpose there is property named: bigxmlreader.xml-reader.remove-namespace-manually-enabled
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, xmlReaderConfig.isNamespaceAware());
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        PathTracker pathTracker = new PathTracker();
        ResultHolder resultHolder = new ResultHolder();
        PayInfo payInfo = null;

        while(xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            PathConfig pathConfig = null;
            switch(event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (xmlPathReaper(xmlStreamReader.getLocalName()).equals("Document")) {
                        break;
                    }
                    pathTracker.addNextElement(xmlPathReaper(xmlStreamReader.getLocalName().trim()));

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
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (xmlPathReaper(xmlStreamReader.getLocalName()).equals("Document")) {
                        break;
                    }
                    pathConfig = pathConfigPerPath.get(pathTracker.getFullTrack());
                    String lastElement = pathTracker.getLastElement();
                    if (lastElement.equals(xmlPathReaper(xmlStreamReader.getLocalName()))) {
                        log.debug(pathTracker.getFullTrack());
                        pathTracker.removeLastElement();
                    }
                    if (isPayInfo(pathConfig)) {
                        resultHolder.addToList(payInfo);
                    }
                    break;
            }
        }

        return resultHolder;
    }

    private void processXmlForHeaderAndFooter(String pathToXmlFile, ResultHolder resultHolder) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        boolean inHeader = true;
        boolean inFooter = false;
        int payInfDepth = 0;

        StringBuilder header = resultHolder.getHeader();
        StringBuilder footer = resultHolder.getFooter();

        while(xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch(event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String elementName = xmlStreamReader.getLocalName();
                    // Check for <ar:PayInf>
                    if ("PayInf".equals(elementName) && "http://www.equens.com/zvs/archive/transaction".equals(xmlStreamReader.getNamespaceURI())) {
                        inHeader = false; // End header processing
                        payInfDepth++;    // Start counting depth of <ar:PayInf>
                    } else if (payInfDepth == 0 && !inFooter) {
                        // Append to header if before <ar:PayInf>
                        header.append("<ar:").append(elementName);
                        appendNamespaces(xmlStreamReader, header);
                        header.append(">");
                    } else if (payInfDepth > 0) {
                        // Increment depth for nested elements within <ar:PayInf>
                        payInfDepth++;
                    } else if (inFooter) {
                        // Append to footer if after last </ar:PayInf>
                        footer.append("<ar:").append(elementName);
                        appendNamespaces(xmlStreamReader, footer);
                        footer.append(">");
                    }
                }
                case XMLStreamConstants.CHARACTERS -> {
                    if (payInfDepth == 0) {
                        // Append text content to header or footer
                        String text = xmlStreamReader.getText().trim();
                        if (inHeader && !text.isEmpty()) {
                            header.append(text);
                        } else if (inFooter && !text.isEmpty()) {
                            footer.append(text);
                        }
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    String endElement = xmlStreamReader.getLocalName();
                    // Check for </ar:PayInf>
                    if ("PayInf".equals(endElement) && "http://www.equens.com/zvs/archive/transaction".equals(xmlStreamReader.getNamespaceURI())) {
                        payInfDepth--; // Decrement depth
                        if (payInfDepth == 0) {
                            inFooter = true; // After last </ar:PayInf>, process footer
                        }
                    } else if (payInfDepth == 0 && !inFooter) {
                        // Append closing tags to header
                        header.append("</ar:").append(endElement).append(">");
                    } else if (payInfDepth == 0 && inFooter) {
                        // Append closing tags to footer
                        footer.append("</ar:").append(endElement).append(">");
                    } else if (payInfDepth > 0) {
                        payInfDepth--; // Decrement nested depth
                    }
                }
            }
        }
    }

    private String xmlPathReaper(String originalPath) {
        if (xmlReaderConfig.isRemoveNamespaceManuallyEnabled()) {
            for (String ns : xmlReaderConfig.getNamespaces()) {
                return originalPath.replace(ns, "");
            }
        }
        return originalPath;
    }

    private static void appendNamespaces(XMLStreamReader reader, StringBuilder builder) {
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            builder.append(" xmlns:").append(reader.getNamespacePrefix(i))
                    .append("=\"").append(reader.getNamespaceURI(i)).append("\"");
        }
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            builder.append(" ").append(reader.getAttributeLocalName(i))
                    .append("=\"").append(reader.getAttributeValue(i)).append("\"");
        }
    }
}
