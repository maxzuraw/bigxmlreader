package pl.bigxml.reader.business;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.config.CsvReaderProperties;
import pl.bigxml.reader.domain.Appearance;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.domain.Processing;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfigFileReader {

    private final CsvReaderProperties csvReaderProperties;

    public List<PathConfig> read(String path) throws Exception {
        List<PathConfig> result = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(csvReaderProperties.getColumnSeparator())
                .withIgnoreQuotations(true)
                .build();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(path)).withCSVParser(parser).build()) {
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
