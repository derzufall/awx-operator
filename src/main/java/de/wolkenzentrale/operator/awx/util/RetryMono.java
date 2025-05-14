package de.wolkenzentrale.operator.awx.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
public class RetryMono<T> {
    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(10);

    private final Mono<T> operation;
    private final String operationName;

    private RetryMono(Mono<T> operation, String operationName) {
        this.operation = operation;
        this.operationName = operationName;
    }

    /**
     * Unit operation (of) - lifts a value into the RetryMono context
     */
    public static <T> RetryMono<T> of(T value, String operationName) {
        return new RetryMono<>(Mono.just(value), operationName);
    }

    /**
     * Unit operation (of) - lifts a Mono into the RetryMono context
     */
    public static <T> RetryMono<T> of(Mono<T> operation, String operationName) {
        return new RetryMono<>(operation, operationName);
    }

    /**
     * Bind operation (flatMap) - transforms the value while maintaining the RetryMono context
     */
    public <R> RetryMono<R> flatMap(Function<T, RetryMono<R>> mapper) {
        return new RetryMono<>(
            operation.flatMap(value -> mapper.apply(value).run()),
            operationName + " -> " + mapper.toString()
        );
    }

    /**
     * Map operation - transforms the value while maintaining the RetryMono context
     */
    public <R> RetryMono<R> map(Function<T, R> mapper) {
        return flatMap(value -> RetryMono.of(mapper.apply(value), operationName));
    }

    /**
     * Executes the operation with retry behavior
     */
    public Mono<T> run() {
        return operation.retryWhen(Retry.backoff(MAX_RETRIES, INITIAL_DELAY)
            .filter(throwable -> {
                if (throwable instanceof WebClientResponseException) {
                    var ex = (WebClientResponseException) throwable;
                    int statusCode = ex.getStatusCode().value();
                    return statusCode >= 400 && statusCode < 600;
                }
                return false;
            })
            .doBeforeRetry(retrySignal -> 
                log.warn("Retrying {} after error. Attempt: {}", 
                    operationName, retrySignal.totalRetries() + 1))
        );
    }
} 