package pl.bigxml.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.bigxml.reader.business.ConfigFileReader;
import pl.bigxml.reader.config.CsvReaderConfig;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.exceptions.CommandLineArgumentsCountMissmatchException;

import java.util.List;

@SpringBootApplication
@EnableConfigurationProperties({
		CsvReaderConfig.class
})
@RequiredArgsConstructor
public class BigXmlReaderApplication implements CommandLineRunner {

	private final ConfigFileReader configFileReader;

	public static void main(String[] args) {
		SpringApplication.run(BigXmlReaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (args.length < 2) {
			throw new CommandLineArgumentsCountMissmatchException();
		}
		System.out.println(args[0]);
		System.out.println(args[1]);
		List<PathConfig> configs = configFileReader.read(args[0]);
		System.out.println(configs.size());
	}


}
