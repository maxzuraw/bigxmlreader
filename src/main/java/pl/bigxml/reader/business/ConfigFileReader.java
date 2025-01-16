package pl.bigxml.reader.business;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.CsvReaderConfig;
import pl.bigxml.reader.domain.PathConfig;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfigFileReader {

    private final CsvReaderConfig csvReaderConfig;

    public List<PathConfig> read(String path) throws Exception {
        List<PathConfig> result = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(csvReaderConfig.getColumnSeparator())
                .withIgnoreQuotations(true)
                .build();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(path)).withCSVParser(parser).build()) {
            String[] line;
            if (csvReaderConfig.isSkipHeader()) {
                reader.readNext();
            }
            while ((line = reader.readNext()) != null) {
                PathConfig pathConfig = PathConfig.builder()
                        .xmlPath(line[0])
                        .fullQualifiedClassName(line[1])
                        .targetName(line[2])
                        .build();
                result.add(pathConfig);
            }
        }
        return result;
    }
}
