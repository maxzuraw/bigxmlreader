package pl.bigxml.reader.exceptions;

public class ChunkProcessingException extends IllegalStateException {

    public ChunkProcessingException(String message) {
        super(message);
    }

    public ChunkProcessingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
