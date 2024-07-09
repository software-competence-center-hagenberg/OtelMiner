package at.scch.freiseisen.ma.trace_collector.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

/**
 * <pre>
 *     global configuration for RabbitMQ to handle exception. We're treating each exception as fatal so we do not
 *     create an endless loop in which RabbitMQ tries to execute the same listener over and over.
 *     In the future we might want to distinct letter boxes for for different kinds of exceptions in order to
 *     analyze them at a later point in time.
 * </pre>
 */
@Slf4j
public class GlobalFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
    @Override
    public boolean isFatal(Throwable t) {
        log.error("Exception occurred which is deemed fatal by Global Fatal Exception Strategy:\n {}", t.getCause().toString());
        return true;
    }
}
