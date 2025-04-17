package at.scch.freiseisen.ma.trace_collector.error;

public class TraceCacheException extends RuntimeException {
    public TraceCacheException(String message, Exception e) {
        super(message, e);
    }
    public TraceCacheException(String message) {
        super(message);
    }
}
