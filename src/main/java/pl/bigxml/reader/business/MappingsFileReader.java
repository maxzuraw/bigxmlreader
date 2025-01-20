package pl.bigxml.reader.business;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.CsvReaderProperties;
import pl.bigxml.reader.domain.MappingsConfig;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MappingsFileReader {

    private final CsvReaderProperties csvReaderProperties;

    @Value("classpath:header_footer_mappings.csv")
    private Resource headerFooterMappingsFile;

    @Value("classpath:payment_mappings.csv")
    private Resource paymentsMappingsFile;

    @SneakyThrows
    public List<MappingsConfig> readHeaderFooterMappings() {
        return read(headerFooterMappingsFile);
    }

    @SneakyThrows
    public List<MappingsConfig> readPaymentMappings() {
        return read(paymentsMappingsFile);
    }

    private List<MappingsConfig> read(Resource resource) throws Exception {
        List<MappingsConfig> result = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(csvReaderProperties.getColumnSeparator())
                .withIgnoreQuotations(true)
                .build();
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream()); CSVReader reader = new CSVReaderBuilder(inputStreamReader).withCSVParser(parser).build()) {
            String[] line;
            if (csvReaderProperties.isSkipHeader()) {
                reader.readNext();
            }
            while ((line = reader.readNext()) != null) {
                MappingsConfig mappingsConfig = MappingsConfig.builder()
                        .xmlPath(line[0])
                        .classCanonicalName(line[1])
                        .targetName(line[2])
                        .build();
                result.add(mappingsConfig);
            }
        }
        return result;
    }
}
