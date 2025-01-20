package pl.bigxml.reader.business.payments;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pl.bigxml.reader.domain.*;
import pl.bigxml.reader.utils.CastingUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Map;
import java.util.function.Function;

import static pl.bigxml.reader.utils.PropertySetter.setProperty;
import static pl.bigxml.reader.utils.SnakeToCamelCase.toCamelCase;

@Slf4j
public class SinglePaymentMapper implements Function<String, Payment> {

    private final ConfigurationMaps maps;
    private final String header;
    private final String footer;

    public SinglePaymentMapper(ConfigurationMaps maps, HeaderFooter headerFooter) {
        this.maps = maps;
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
        Map<String, MappingsConfig> perXmlPath = maps.getConfigurationPerXmlPath();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                pathTracker.addNextElement(reader.getLocalName());
                for (int i =0; i < reader.getAttributeCount(); i++) {
                    var fullTrack = getTrackWithDynamicAttribute(reader, i, pathTracker);
                    String value = reader.getAttributeValue(i);
                    setValueInPayment(perXmlPath, fullTrack, value, payment);
                }
            } else if (event == XMLStreamConstants.CHARACTERS) {
                setValueInPayment(perXmlPath, pathTracker.getFullTrack(), reader.getText(), payment);
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

    private static void setValueInPayment(Map<String, MappingsConfig> perXmlPath, String fullTrack, String value, Payment payment) throws ClassNotFoundException {
        var mappingsConfig = perXmlPath.get(fullTrack);
        if (mappingsConfig != null) {
            Class<?> aClass = Class.forName(mappingsConfig.getClassCanonicalName());
            var setterMethod = toCamelCase(mappingsConfig.getTargetName());
            Object object = CastingUtils.toObject(value, aClass);
            setProperty(payment, setterMethod, object);
        }
    }

    private static String getTrackWithDynamicAttribute(XMLStreamReader reader, int i, PathTracker pathTracker) {
        String attributeName = reader.getAttributeLocalName(i);
        String attributeNameAdjusted = "[" + attributeName + "]";
        var fullTrack = pathTracker.getFullTrack();
        return fullTrack + "." + attributeNameAdjusted;
    }
}
