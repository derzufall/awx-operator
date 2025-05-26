package de.wolkenzentrale.operator.awx.config;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestOpenTelemetryConfig {

    @Bean
    @Primary
    public Tracer tracer() {
        Tracer tracer = Mockito.mock(Tracer.class);
        Span span = Mockito.mock(Span.class);
        Scope scope = Mockito.mock(Scope.class);
        SpanBuilder spanBuilder = Mockito.mock(SpanBuilder.class);
        
        Mockito.when(tracer.spanBuilder(Mockito.anyString())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.setSpanKind(Mockito.any(SpanKind.class))).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.setAttribute(Mockito.anyString(), Mockito.any())).thenReturn(spanBuilder);
        Mockito.when(spanBuilder.startSpan()).thenReturn(span);
        Mockito.when(span.makeCurrent()).thenReturn(scope);
        
        return tracer;
    }

    @Bean
    @Primary
    public Meter meter() {
        Meter meter = Mockito.mock(Meter.class);
        LongCounter counter = Mockito.mock(LongCounter.class);
        LongCounterBuilder counterBuilder = Mockito.mock(LongCounterBuilder.class);
        
        Mockito.when(meter.counterBuilder(Mockito.anyString())).thenReturn(counterBuilder);
        Mockito.when(counterBuilder.setDescription(Mockito.anyString())).thenReturn(counterBuilder);
        Mockito.when(counterBuilder.build()).thenReturn(counter);
        
        return meter;
    }
} 