package pl.bigxml.reader.business.payments;

import lombok.SneakyThrows;
import pl.bigxml.reader.domain.Payment;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.function.Function;

public class SinglePaymentMapper implements Function<String, Payment> {

    @SneakyThrows
    @Override
    public Payment apply(String s) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(s));

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                System.out.println("Start Element: " + reader.getLocalName());
            } else if (event == XMLStreamConstants.ATTRIBUTE) {
                System.out.println("Attribute Element: " + reader.getLocalName());
            } else if (event == XMLStreamConstants.CHARACTERS) {
                System.out.println("Text: " + reader.getText());
            } else if (event == XMLStreamConstants.CDATA) {
                System.out.println("CDATA: " + reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                System.out.println("End Element: " + reader.getLocalName());
            }
        }
        reader.close();

        return null;
    }
}
