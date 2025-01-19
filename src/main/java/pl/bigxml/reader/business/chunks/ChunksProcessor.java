package pl.bigxml.reader.business.chunks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.XmlReaderProperties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunksProcessor {

    private final XmlReaderProperties readerConfig;

    public void process(String pathToXmlFile, int chunkSize, ChunkProcessingCallback chunkProcessingCallback) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        StringBuilder currentChunk = new StringBuilder();
        int count = 0;
        int currentCount = 0;

        long startTime = System.nanoTime();

        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();

            if (event == XMLStreamConstants.START_ELEMENT && readerConfig.getBodyNodeLocalName().equals(xmlStreamReader.getLocalName())) {
                String payInfElement = readElement(xmlStreamReader, readerConfig.getBodyNodeLocalName());
                currentChunk.append(payInfElement).append("\n");
                count++;
                currentCount++;

                if (count % chunkSize == 0 ) {
                    chunkProcessingCallback.apply(currentChunk.toString().trim(), currentCount);
                    currentChunk.setLength(0); // Clear the chunk
                    currentCount = 0;
                }
            }
        }
        if (!currentChunk.isEmpty()) {
            chunkProcessingCallback.apply(currentChunk.toString().trim(), currentCount);
        }

        long stopTime = System.nanoTime();

        log.info("Processed total of {} PayInf elements in {} seconds.", count, toSeconds(stopTime - startTime));
        xmlStreamReader.close();
    }

    private static String readElement(XMLStreamReader reader, String elementName) throws Exception {
        StringBuilder elementBuilder = new StringBuilder();
        elementBuilder.append("<");

        appendPrefixIfExists(reader, elementBuilder);

        elementBuilder.append(elementName);

        appendNamespacesIfExists(reader, elementBuilder);
        appendAttributesIfExists(reader, elementBuilder);

        elementBuilder.append(elementName).append(">");
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS) {
                elementBuilder.append(reader.getText());
            } else if (event == XMLStreamConstants.CDATA) {
                elementBuilder.append("<![CDATA[")
                        .append(reader.getText())
                        .append("]]>");
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                elementBuilder.append("<");
                appendPrefixIfExists(reader, elementBuilder);
                elementBuilder.append(reader.getLocalName());
                appendNamespacesIfExists(reader, elementBuilder);
                appendAttributesIfExists(reader, elementBuilder);
                elementBuilder.append(">");
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (elementName.equals(reader.getLocalName())) {
                    elementBuilder.append("</");
                    appendPrefixIfExists(reader, elementBuilder);
                    elementBuilder.append(elementName);
                    elementBuilder.append(">");
                    break;
                } else {
                    elementBuilder.append("</");
                    appendPrefixIfExists(reader, elementBuilder);
                    elementBuilder.append(reader.getLocalName());
                    elementBuilder.append(">");
                }
            }
        }
        return elementBuilder.toString();
    }

    private static void appendPrefixIfExists(XMLStreamReader reader, StringBuilder builder) {
        if (reader.getPrefix() != null && !reader.getPrefix().isEmpty()) {
            builder.append(reader.getPrefix()).append(":");
        }
    }

    private static void appendNamespacesIfExists(XMLStreamReader reader, StringBuilder builder) {
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            builder.append(" xmlns");
            if (reader.getNamespacePrefix(i) != null) {
                builder.append(":").append(reader.getNamespacePrefix(i));
            }
            builder.append("=\"").append(reader.getNamespaceURI(i)).append("\"");
        }
    }

    private static void appendAttributesIfExists(XMLStreamReader reader, StringBuilder builder) {
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
    }
}
