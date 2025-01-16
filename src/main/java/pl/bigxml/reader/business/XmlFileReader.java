package pl.bigxml.reader.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.XmlReaderConfig;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.domain.PathConfigMaps;
import pl.bigxml.reader.domain.PathTracker;
import pl.bigxml.reader.domain.ResultMap;

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

    public ResultMap read(String pathToXmlFile, PathConfigMaps pathConfigMaps) throws FileNotFoundException, XMLStreamException {
        Map<String, PathConfig> pathConfigPerPath = pathConfigMaps.getConfigMap();


        XMLInputFactory factory = XMLInputFactory.newInstance();
        // NOTE: for some reason this property does not work on my environment
        // so if it does not work, there is manual processing of elements to omit namespaces
        // for that purpose there is property named: bigxmlreader.xml-reader.remove-namespace-manually-enabled
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        PathTracker pathTracker = new PathTracker();

        ResultMap resultMap = new ResultMap();

        while(xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch(event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (xmlPathReaper(xmlStreamReader.getLocalName()).equals("Document")) {
                        break;
                    }
                    pathTracker.addNextElement(xmlPathReaper(xmlStreamReader.getLocalName().trim()));
                    break;
                case XMLStreamConstants.CHARACTERS:
                    PathConfig pathConfig = pathConfigPerPath.get(pathTracker.getFullTrack());
                    if (pathConfig != null) {
                        resultMap.put(pathConfig.getTargetName(), xmlStreamReader.getText().trim());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (xmlPathReaper(xmlStreamReader.getLocalName()).equals("Document")) {
                        break;
                    }
                    String lastElement = pathTracker.getLastElement();
                    if (lastElement.equals(xmlPathReaper(xmlStreamReader.getLocalName()))) {
                        log.debug(pathTracker.getFullTrack());
                        pathTracker.removeLastElement();
                    }
                    break;
            }
        }

        return resultMap;
    }

    private String xmlPathReaper(String originalPath) {
        if (xmlReaderConfig.isRemoveNamespaceManuallyEnabled()) {
            for (String ns : xmlReaderConfig.getNamespaces()) {
                return originalPath.replace(ns, "");
            }
        }
        return originalPath;
    }
}
