package pl.bigxml.reader.exceptions;

public class CommandLineArgumentsCountMissmatchException extends IllegalArgumentException{

    private static final String ARGUMENTS_MISSMATCH_EXCEPTION = """
			Application needs exactly 2 arguments.
			First is path to configuration file in csv format.
			Second is path to xml file, to process.
			""";

    public CommandLineArgumentsCountMissmatchException() {
        super(ARGUMENTS_MISSMATCH_EXCEPTION);
    }
}
