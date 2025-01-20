package pl.bigxml.reader.business.payments;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pl.bigxml.reader.domain.HeaderFooter;
import pl.bigxml.reader.domain.MappingsConfig;
import pl.bigxml.reader.domain.PathTracker;
import pl.bigxml.reader.domain.Payment;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class SinglePaymentMapper implements Function<String, Payment> {

    private final List<MappingsConfig> configs;
    private final String header;
    private final String footer;

    public SinglePaymentMapper(List<MappingsConfig> configs, HeaderFooter headerFooter) {
        this.configs = configs;
        this.header = headerFooter.getHeader().toString();
        this.footer = headerFooter.getFooter().toString();
    }

    @SneakyThrows
    @Override
    public Payment apply(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        sb.append(s);
        sb.append(footer);

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(sb.toString()));

        Payment payment = Payment.builder().build();
        PathTracker pathTracker = new PathTracker();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                pathTracker.addNextElement(reader.getLocalName());
                System.out.println("Start Element: " + reader.getLocalName());
                for (int i =0; i < reader.getAttributeCount(); i++) {
                    String attributeName = reader.getAttributeLocalName(i);
                    String attributeValue = reader.getAttributeValue(i);
                    String attributeNameAdjusted = "[" + attributeName + "]";
                    var fullTrack = pathTracker.getFullTrack();
                    String fullTrackWithAttribute = fullTrack + "." + attributeNameAdjusted;
                    System.out.println("Attribute Element: " + fullTrackWithAttribute);
                }
            } else if (event == XMLStreamConstants.CHARACTERS) {
                System.out.println("Text: " + reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                String lastElement = pathTracker.getLastElement();
                if (lastElement.equals(reader.getLocalName())) {
                    log.debug(pathTracker.getFullTrack());
                    pathTracker.removeLastElement();
                }
            }
        }
        reader.close();
        return payment;
    }
}
