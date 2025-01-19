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
import pl.bigxml.reader.domain.Appearance;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.domain.Processing;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfigFileReader {

    private final CsvReaderProperties csvReaderProperties;

    @Value("classpath:config.csv")
    private Resource resourceFile;

    public List<PathConfig> read() throws Exception {
        List<PathConfig> result = new ArrayList<>();
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
                PathConfig pathConfig = PathConfig.builder()
                        .xmlPath(line[0])
                        .fullQualifiedClassName(line[1])
                        .targetName(line[2])
                        .appearance(Appearance.valueOf(line[3]))
                        .processing(Processing.valueOf(line[4]))
                        .build();
                result.add(pathConfig);
            }
        }
        return result;
    }
}
