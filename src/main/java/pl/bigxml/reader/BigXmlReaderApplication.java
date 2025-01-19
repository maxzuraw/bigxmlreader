package pl.bigxml.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.bigxml.reader.business.*;
import pl.bigxml.reader.business.chunks.ChunkProcessingCallback;
import pl.bigxml.reader.business.chunks.ChunksProcessor;
import pl.bigxml.reader.business.headerandfooter.HeaderAndFooterProcessor;
import pl.bigxml.reader.business.payments.PaymentsProcessor;
import pl.bigxml.reader.business.payments.SinglePaymentMapper;
import pl.bigxml.reader.business.payments.StorageCallback;
import pl.bigxml.reader.config.CsvReaderProperties;
import pl.bigxml.reader.config.XmlChunkWriterProperties;
import pl.bigxml.reader.config.XmlReaderProperties;
import pl.bigxml.reader.domain.PathConfig;
import pl.bigxml.reader.domain.PathConfigMaps;
import pl.bigxml.reader.domain.ResultHolder;
import pl.bigxml.reader.exceptions.CommandLineArgumentsCountMissmatchException;

import java.util.List;

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

	private final ConfigFileReader configFileReader;
	private final XmlReaderProperties xmlReaderProperties;
	private final XmlChunkWriterProperties xmlChunkWriterProperties;

	private final HeaderAndFooterProcessor headerAndFooterProcessor;
	private final ChunksProcessor chunksProcessor;
	private final PaymentsProcessor paymentsProcessor;


	public static void main(String[] args) {
		SpringApplication.run(BigXmlReaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (args.length < 1) {
			throw new CommandLineArgumentsCountMissmatchException();
		}
		List<PathConfig> configs = configFileReader.read();
		PathConfigMaps pathConfigMaps = new PathConfigMaps(configs);


		// 1. First processing: get header and footer as strings & get header values
		long startTime = System.nanoTime();
		ResultHolder resultHolder = headerAndFooterProcessor.read(args[0], pathConfigMaps);
		long stopTime = System.nanoTime();
		log.info("Processing for header/footer string and header values done in seconds: {}", toSeconds(stopTime - startTime));


		// 2. Second processing: chunk xml into smaller xmls
		startTime = System.nanoTime();
		chunksProcessor.process(args[0], xmlReaderProperties.getChunkSize(), new ChunkProcessingCallback(
				resultHolder.getHeader().toString(),
				resultHolder.getFooter().toString(),
				xmlChunkWriterProperties.getTargetFolder(),
				xmlChunkWriterProperties.getTargetFilePrefix())
		);
		stopTime = System.nanoTime();
		log.info("Chunks read in seconds: {}", toSeconds(stopTime - startTime));


		// 3. Third processing: map payments and store them somewhere
		paymentsProcessor.process(args[0], xmlReaderProperties.getChunkSize(), new SinglePaymentMapper(), new StorageCallback());


		// NOTE: write some header values to show how to access them
		writeSomeHeaderValues(pathConfigMaps, resultHolder);
	}

	private static void writeSomeHeaderValues(PathConfigMaps pathConfigMaps, ResultHolder resultHolder) throws ClassNotFoundException {
		PathConfig pathConfig = pathConfigMaps.getResultMap().get("versionInterface");
		var version = resultHolder.getMapValueByKey(pathConfig.getTargetName(), Class.forName(pathConfig.getFullQualifiedClassName()));
		log.info("{}", version);
		pathConfig = pathConfigMaps.getResultMap().get("archivizationDate");
		var archivizationDate = resultHolder.getMapValueByKey(pathConfig.getTargetName(), Class.forName(pathConfig.getFullQualifiedClassName()));
		log.info("{}", archivizationDate);
	}
}
