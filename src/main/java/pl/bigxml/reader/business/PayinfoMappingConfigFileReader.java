package pl.bigxml.reader.business;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.CsvReaderProperties;
import pl.bigxml.reader.domain.PayinfoMappingConfig;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PayinfoMappingConfigFileReader {

    private final CsvReaderProperties csvReaderProperties;

    @Value("classpath:payinfo_mapping.csv")
    private Resource resourceFile;

    public List<PayinfoMappingConfig> read() throws Exception {
        List<PayinfoMappingConfig> result = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(csvReaderProperties.getColumnSeparator())
                .withIgnoreQuotations(true)
                .build();
        try (InputStreamReader inputStreamReader = new InputStreamReader(resourceFile.getInputStream()); CSVReader reader = new CSVReaderBuilder(inputStreamReader).withCSVParser(parser).build()) {
            String[] line;
            if (csvReaderProperties.isSkipHeader()) {
                reader.readNext();
            }
            while ((line = reader.readNext()) != null) {
                PayinfoMappingConfig config = PayinfoMappingConfig.builder()
                        .xmlPath(line[0])
                        .targetPropertyName(line[1])
                        .build();
                result.add(config);
            }
        }
        return result;
    }

}
