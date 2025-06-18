package at.scch.freiseisen.ma.model_generator.error;

public class TraceStringConversionException extends RuntimeException{

    public TraceStringConversionException(Exception e) {
        super(e);
    }
}
