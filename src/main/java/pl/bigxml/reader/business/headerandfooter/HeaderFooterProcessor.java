package pl.bigxml.reader.business.headerandfooter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.XmlReaderProperties;
import pl.bigxml.reader.domain.HeaderFooter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderFooterProcessor {

    private final XmlReaderProperties xmlReaderProperties;

    public HeaderFooter read(String pathToXmlFile) throws FileNotFoundException, XMLStreamException {
        return readHeaderFooter(pathToXmlFile);
    }

    private HeaderFooter readHeaderFooter(String pathToXmlFile) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, xmlReaderProperties.isNamespaceAware());
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        HeaderFooter headerFooter = new HeaderFooter();

        StringBuilder header = new StringBuilder();
        StringBuilder footer = new StringBuilder();
        StringBuilder currentSection = header;
        boolean insidePayInf = false;
        while(xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            switch(event) {
                case XMLStreamConstants.START_ELEMENT:
                    String localName = xmlStreamReader.getLocalName();
                    if (xmlReaderProperties.getBodyNodeLocalName().equals(localName)
                            && xmlReaderProperties.getBodyNodeNamespaceUri().equals(xmlStreamReader.getNamespaceURI())) {
                        insidePayInf = true;
                        currentSection = footer;
                    }
                    if (!insidePayInf) {
                        appendStartElement(xmlStreamReader, currentSection);
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (!insidePayInf) {
                        currentSection.append(xmlStreamReader.getText());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (xmlReaderProperties.getBodyNodeLocalName().equals(xmlStreamReader.getLocalName())
                            && xmlReaderProperties.getBodyNodeNamespaceUri().equals(xmlStreamReader.getNamespaceURI())) {
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
