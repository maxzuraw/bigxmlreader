package pl.bigxml.reader.business.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.business.BodyElementReader;
import pl.bigxml.reader.config.XmlReaderProperties;
import pl.bigxml.reader.domain.Payment;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentsProcessor {

    private final XmlReaderProperties readerConfig;

    public void process(String pathToXmlFile, int chunkSize, SinglePaymentMapper singlePaymentMapper, StorageCallback storageCallback) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new FileReader(pathToXmlFile));
        List<Payment> payments = new ArrayList<>();
        int count = 0;
        long startTime = System.nanoTime();
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            if (event == XMLStreamConstants.START_ELEMENT && readerConfig.getBodyNodeLocalName().equals(xmlStreamReader.getLocalName())) {
                String payInfElement = BodyElementReader.readElement(xmlStreamReader, readerConfig.getBodyNodeLocalName());
                Payment payment = singlePaymentMapper.apply(payInfElement);
                if (payment != null) {
                    payments.add(payment);
                }
                count++;
                if (count % chunkSize == 0 ) {
                    if (!payments.isEmpty()) {
                        storageCallback.apply(payments);
                    }
                    payments.clear();
                }
            }
        }
        if (!payments.isEmpty()) {
            storageCallback.apply(payments);
        }

        long stopTime = System.nanoTime();

        log.info("Processed total of {} PayInf elements in {} seconds.", count, toSeconds(stopTime - startTime));
        xmlStreamReader.close();
    }
}
