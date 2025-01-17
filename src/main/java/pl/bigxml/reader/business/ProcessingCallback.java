package pl.bigxml.reader.business;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BiFunction;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
public class ProcessingCallback implements BiFunction<String, Integer, Void> {
    @Override
    public Void apply(String chunk, Integer processedCount) {
        long startTime = System.nanoTime();
        log.debug("==================== START PROCESSING ====================");
        log.info("Processing chunk with {} PayInf elements.", processedCount);
        log.debug("Chunk: {}", chunk);
        log.debug("==================== END PROCESSING ====================");
        long stopTime = System.nanoTime();
        log.info("chunk of {} payments processed in {} seconds.", processedCount, toSeconds(stopTime - startTime));
        return null;
    }
}
