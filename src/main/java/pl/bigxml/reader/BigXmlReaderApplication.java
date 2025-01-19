package pl.bigxml.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.bigxml.reader.business.ConfigFileReader;
import pl.bigxml.reader.business.ProcessingCallback;
import pl.bigxml.reader.business.XmlFileReader;
import pl.bigxml.reader.business.XmlPaymentsProcessor;
import pl.bigxml.reader.config.CsvReaderConfig;
import pl.bigxml.reader.config.XmlReaderConfig;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.domain.PathConfigMaps;
import pl.bigxml.reader.domain.ResultHolder;
import pl.bigxml.reader.exceptions.CommandLineArgumentsCountMissmatchException;

import java.util.List;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({
		CsvReaderConfig.class,
		XmlReaderConfig.class
})
@RequiredArgsConstructor
public class BigXmlReaderApplication implements CommandLineRunner {

	private final ConfigFileReader configFileReader;
	private final XmlFileReader xmlFileReader;
	private final XmlPaymentsProcessor paymentsProcessor;
	private final XmlReaderConfig xmlReaderConfig;

	public static void main(String[] args) {
		SpringApplication.run(BigXmlReaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (args.length < 2) {
			throw new CommandLineArgumentsCountMissmatchException();
		}
		List<PathConfig> configs = configFileReader.read(args[0]);
		PathConfigMaps pathConfigMaps = new PathConfigMaps(configs);

		long startTime = System.nanoTime();
		ResultHolder resultHolder = xmlFileReader.read(args[1], pathConfigMaps);
		long stopTime = System.nanoTime();
		log.info("Xml file read in seconds: {}", toSeconds(stopTime - startTime));


		PathConfig pathConfig = pathConfigMaps.getResultMap().get("versionInterface");
		var version = resultHolder.getMapValueByKey(pathConfig.getTargetName(), Class.forName(pathConfig.getFullQualifiedClassName()));
		log.info("{}", version);
		pathConfig = pathConfigMaps.getResultMap().get("archivizationDate");
		var archivizationDate = resultHolder.getMapValueByKey(pathConfig.getTargetName(), Class.forName(pathConfig.getFullQualifiedClassName()));
		log.info("{}", archivizationDate);

		log.info("{}", resultHolder.getHeader().toString());
		log.info("{}", resultHolder.getFooter().toString());


		paymentsProcessor.process(args[1], xmlReaderConfig.getChunkSize(), new ProcessingCallback());

	}
}
