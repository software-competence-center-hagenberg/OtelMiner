package at.scch.freiseisen.ma.model_generator.error;

public class ModelGenerationException extends RuntimeException {
    public ModelGenerationException(String message, Exception e) {
        super(message, e);
    }
    public ModelGenerationException(String message) {
        super(message);
    }
}
