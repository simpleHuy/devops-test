package org.springframework.samples.petclinic.customers.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricConfig {
  @Bean
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
  @Bean
  MeterFilter traceIdTaggingFilter() {
      return MeterFilter.commonTags(
          Tags.of(Tag.of("traceId", Optional.ofNullable(MDC.get("traceId")).orElse("unknown")))
      );
  }
  @Bean
  public Filter traceIdFilter() {
      return (request, response, chain) -> {
          // Extract trace ID from headers
          String traceId = extractTraceIdFromHeaders((HttpServletRequest) request);
          try {
              // Set in MDC before processing
              MDC.put("traceId", traceId);
              chain.doFilter(request, response);
          } finally {
              MDC.remove("traceId");
          }
      };
  }

  private String extractTraceIdFromHeaders(HttpServletRequest request) {
      // Common header names for trace IDs
      String[] possibleHeaders = {
          "X-B3-TraceId",       // Zipkin B3
          "traceparent",        // W3C Trace Context
          "X-Request-ID",       // Common custom header
          "X-Correlation-ID"    // Common custom header
      };
      
      for (String headerName : possibleHeaders) {
          String value = request.getHeader(headerName);
          if (value != null && !value.isEmpty()) {
              // For W3C traceparent, extract the trace ID portion
              if (headerName.equals("traceparent") && value.contains("-")) {
                  String[] parts = value.split("-");
                  if (parts.length >= 2) {
                      return parts[1];
                  }
              }
              return value;
          }
      }
      
      return "unknown";
  }
}
