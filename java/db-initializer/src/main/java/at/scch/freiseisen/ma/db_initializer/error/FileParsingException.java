package at.scch.freiseisen.ma.db_initializer.error;

public class FileParsingException extends RuntimeException {
    public FileParsingException(String filePath) {
        super("Error parsing file: " + filePath);
    }
    public FileParsingException(String filePath, Exception cause) {
        super("Error parsing file: " + filePath, cause);
    }
}
