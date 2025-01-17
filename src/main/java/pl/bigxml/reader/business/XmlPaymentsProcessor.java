package pl.bigxml.reader.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlPaymentsProcessor {

    public void process(String pathToXmlFile, int chunkSize, ProcessingCallback processingCallback) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));

        StringBuilder currentChunk = new StringBuilder();
        int count = 0;
        int currentCount = 0;

        long startTime = System.nanoTime();

        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();

            if (event == XMLStreamConstants.START_ELEMENT && "PayInf".equals(xmlStreamReader.getLocalName())) {
                String payInfElement = readElement(xmlStreamReader, "PayInf");
                currentChunk.append(payInfElement).append("\n");
                count++;
                currentCount++;

                if (count % chunkSize == 0 ) {
                    processingCallback.apply(currentChunk.toString().trim(), currentCount);
                    currentChunk.setLength(0); // Clear the chunk
                    currentCount = 0;
                }
            }
        }
        if (!currentChunk.isEmpty()) {
            processingCallback.apply(currentChunk.toString().trim(), currentCount);
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
