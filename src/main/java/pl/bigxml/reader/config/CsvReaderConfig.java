package pl.bigxml.reader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bigxmlreader.csv-reader")
@Data
public class CsvReaderConfig {

    private boolean skipHeader;
    private char columnSeparator;
}
