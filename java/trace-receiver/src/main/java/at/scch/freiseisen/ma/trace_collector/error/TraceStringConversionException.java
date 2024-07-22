package at.scch.freiseisen.ma.trace_collector.error;

public class TraceStringConversionException extends RuntimeException{

    public TraceStringConversionException(Exception e) {
        super(e);
    }
}
