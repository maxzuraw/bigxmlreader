package pl.bigxml.reader.business.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.business.chunks.ChunkProcessingCallback;
import pl.bigxml.reader.config.XmlReaderProperties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentsProcessor {

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
        elementBuilder.append("<").append(elementName).append(">");
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS) {
                elementBuilder.append(reader.getText());
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                elementBuilder.append("<").append(reader.getLocalName()).append(">");
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (elementName.equals(reader.getLocalName())) {
                    elementBuilder.append("</").append(elementName).append(">");
                    break;
                } else {
                    elementBuilder.append("</").append(reader.getLocalName()).append(">");
                }
            }
        }
        return elementBuilder.toString();
    }
}
