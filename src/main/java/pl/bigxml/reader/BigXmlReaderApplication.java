package pl.bigxml.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.bigxml.reader.business.MappingsFileReader;
import pl.bigxml.reader.business.chunks.ChunkProcessingCallback;
import pl.bigxml.reader.business.chunks.ChunksProcessor;
import pl.bigxml.reader.business.headerandfooter.HeaderFooterProcessor;
import pl.bigxml.reader.business.payments.SinglePaymentMapper;
import pl.bigxml.reader.business.payments.StorageCallback;
import pl.bigxml.reader.business.payments.ValuesProcessor;
import pl.bigxml.reader.config.CsvReaderProperties;
import pl.bigxml.reader.config.XmlChunkWriterProperties;
import pl.bigxml.reader.config.XmlReaderProperties;
import pl.bigxml.reader.domain.ConfigurationMaps;
import pl.bigxml.reader.domain.HeaderFooter;
import pl.bigxml.reader.domain.MappingsConfig;
import pl.bigxml.reader.exceptions.CommandLineArgumentsCountMissmatchException;

import java.util.List;
import java.util.Map;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({
		CsvReaderProperties.class,
		XmlReaderProperties.class,
		XmlChunkWriterProperties.class
})
@RequiredArgsConstructor
public class BigXmlReaderApplication implements CommandLineRunner {

	private final MappingsFileReader mappingsFileReader;
	private final XmlReaderProperties xmlReaderProperties;
	private final XmlChunkWriterProperties xmlChunkWriterProperties;

	private final HeaderFooterProcessor headerFooterProcessor;
	private final ChunksProcessor chunksProcessor;
	private final ValuesProcessor valuesProcessor;


	public static void main(String[] args) {
		SpringApplication.run(BigXmlReaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (args.length < 1) {
			throw new CommandLineArgumentsCountMissmatchException();
		}
		// 1. First processing: get header and footer as strings & get header values
		long startTime = System.nanoTime();
		HeaderFooter headerFooter = headerFooterProcessor.read(args[0]);
		long stopTime = System.nanoTime();
		log.info("Processing for header/footer string and header values done in seconds: {}", toSeconds(stopTime - startTime));


		// 2. Second processing: chunk xml into smaller xmls
		startTime = System.nanoTime();
		chunksProcessor.process(args[0], xmlReaderProperties.getChunkSize(), new ChunkProcessingCallback(
				headerFooter.getHeader().toString(),
				headerFooter.getFooter().toString(),
				xmlChunkWriterProperties.getTargetFolder(),
				xmlChunkWriterProperties.getTargetFilePrefix())
		);
		stopTime = System.nanoTime();
		log.info("Chunks read in seconds: {}", toSeconds(stopTime - startTime));


		// 3. Third processing: map payments and store them somewhere
		List<MappingsConfig> paymentsConfig = mappingsFileReader.readPaymentMappings();
		ConfigurationMaps maps = new ConfigurationMaps(paymentsConfig);
		SinglePaymentMapper singlePaymentMapper = new SinglePaymentMapper(maps, headerFooter);
		valuesProcessor.process(
				args[0],
				xmlReaderProperties.getChunkSize(),
				singlePaymentMapper,
				new StorageCallback()
		);


		readHeaderFooterMapValues(headerFooter);
	}

	private void readHeaderFooterMapValues(HeaderFooter headerFooter) throws ClassNotFoundException {
		var mappingsConfigs = mappingsFileReader.readHeaderFooterMappings();
		ConfigurationMaps maps = new ConfigurationMaps(mappingsConfigs);
		Map<String, MappingsConfig> configurationPerTargetField = maps.getConfigurationPerTargetField();
		for (Map.Entry<String, MappingsConfig> entry : configurationPerTargetField.entrySet()) {
			Class<?> clazz = Class.forName(entry.getValue().getClassCanonicalName());
			Object value = headerFooter.getFromMap(entry.getKey(), clazz);
			log.info("HeaderFooter. Key: {}, Value: {}, Class: {}", entry.getKey(), value, value.getClass().getCanonicalName());
		}
	}
}
