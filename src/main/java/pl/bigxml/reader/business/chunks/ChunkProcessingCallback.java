package pl.bigxml.reader.business.chunks;

import lombok.extern.slf4j.Slf4j;
import pl.bigxml.reader.exceptions.ChunkProcessingException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiFunction;

import static pl.bigxml.reader.utils.NanoToSeconds.toSeconds;

@Slf4j
public class ChunkProcessingCallback implements BiFunction<String, Integer, Void> {

    private final String header;
    private final String footer;

    private final String targetFolder;

    private final String targetFilePrefix;

    public ChunkProcessingCallback(String header, String footer, String targetFolder, String targetFilePrefix) {
        this.header = header;
        this.footer = footer;
        this.targetFolder = targetFolder;
        this.targetFilePrefix = targetFilePrefix;
    }

    @Override
    public Void apply(String chunk, Integer processedCount) {

        long startTime = System.nanoTime();
        File folder = new File(targetFolder);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                log.debug("Folder created: " + targetFolder);
            } else {
                throw new ChunkProcessingException("Failed to create folder: " + targetFolder);
            }
        }
        String fileName = targetFilePrefix + startTime + ".xml";
        File file = new File(folder, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(header);
            writer.write(chunk);
            var builder = replaceTransactionNumbers(new StringBuilder(footer), processedCount);
            writer.write("\n");
            writer.write(builder.toString());
            log.info("File written successfully to: {}", file.getAbsolutePath());
        } catch (IOException e) {
            throw new ChunkProcessingException("Error writing to file: " + e.getMessage(), e);
        }
        long stopTime = System.nanoTime();
        log.info("File {} stored in {} seconds.", fileName, toSeconds(stopTime - startTime));
        return null;
    }

    private static StringBuilder replaceTransactionNumbers(StringBuilder sb, int transactionsNumber) {
        String[] lines = sb.toString().split("\\r?\\n");
        if (lines.length > 0) {
            lines[0] = lines[0].replaceFirst("\\d+", String.valueOf(transactionsNumber));
        }

        // Rebuild the StringBuilder with updated lines
        StringBuilder updatedSb = new StringBuilder();
        for (String line : lines) {
            updatedSb.append(line).append(System.lineSeparator());
        }

        return updatedSb;
    }
}
