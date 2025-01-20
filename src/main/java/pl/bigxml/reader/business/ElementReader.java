package pl.bigxml.reader.business;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElementReader {

    public static String readElement(XMLStreamReader reader, String elementName) throws Exception {
        StringBuilder elementBuilder = new StringBuilder();
        elementBuilder.append("<");

        appendPrefixIfExists(reader, elementBuilder);

        elementBuilder.append(elementName);

        appendNamespacesIfExists(reader, elementBuilder);
        appendAttributesIfExists(reader, elementBuilder);

        elementBuilder.append(">");
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS) {
                String recodedText = reader.getText().replace("<", "&lt;").replace(">", "&gt;");
                elementBuilder.append(recodedText);
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

    public static void appendPrefixIfExists(XMLStreamReader reader, StringBuilder builder) {
        if (reader.getPrefix() != null && !reader.getPrefix().isEmpty()) {
            builder.append(reader.getPrefix()).append(":");
        }
    }

    public static void appendNamespacesIfExists(XMLStreamReader reader, StringBuilder builder) {
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            builder.append(" xmlns");
            if (reader.getNamespacePrefix(i) != null && Objects.equals(reader.getLocalName(), "Document")) {
                builder.append(":").append(reader.getNamespacePrefix(i));
            }
            builder.append("=\"").append(reader.getNamespaceURI(i)).append("\"");
        }
    }

    public static void appendAttributesIfExists(XMLStreamReader reader, StringBuilder builder) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            builder.append(" ")
                    .append(reader.getAttributePrefix(i) != null && !reader.getAttributePrefix(i).isEmpty()
                            ? reader.getAttributePrefix(i)
                            : "")
                    .append(reader.getAttributeLocalName(i))
                    .append("=\"")
                    .append(reader.getAttributeValue(i))
                    .append("\"");
        }
    }
}
