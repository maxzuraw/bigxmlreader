package pl.bigxml.reader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bigxmlreader.csv-reader")
@Data
public class CsvReaderProperties {

    private boolean skipHeader;
    private char columnSeparator;
}
