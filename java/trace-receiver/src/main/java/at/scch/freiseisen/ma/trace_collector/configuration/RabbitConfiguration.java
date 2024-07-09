package at.scch.freiseisen.ma.trace_collector.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Configuration
public class RabbitConfiguration {

    @Value("${open_telemetry.exporter.routing_key.traces}")
    private String routingKeyTraces;
    @Value("${open_telemetry.exporter.routing_key.metrics}")
    private String routingKeyMetrics;
    @Value("${open_telemetry.exporter.routing_key.logs}")
    private String routingKeyLogs;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(globalFatalExceptionStrategy());
    }

    @Bean
    FatalExceptionStrategy globalFatalExceptionStrategy() {
        return new GlobalFatalExceptionStrategy();
    }

    @Bean
    public MessageConverter messageConverter() {
        SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();
        simpleMessageConverter.addAllowedListPatterns("*");
        return simpleMessageConverter;
    }

    @Bean
    public Queue traceQueue() {
        return new Queue(routingKeyTraces);
    }

    @Bean
    public Queue metricQueue() {
        return new Queue(routingKeyMetrics);
    }

    @Bean
    public Queue logQueue() {
        return new Queue(routingKeyLogs);
    }
}
