package org.springframework.samples.petclinic.visits.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class MetricConfig {
  @Bean
  TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
  
  @Bean
  @Order(1) // Make sure this runs before metrics are collected
  public Filter traceIdFilter() {
    return new Filter() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
          throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = extractTraceIdFromHeaders(httpRequest);
        MDC.put("traceId", traceId);
        try {
          chain.doFilter(request, response);
        } finally {
          MDC.remove("traceId");
        }
      }
    };
  }

  // This MeterFilter will be applied to each meter as it's registered
  @Bean
  public MeterFilter addTraceIdToMetrics() {
    return new MeterFilter() {
      @Override
      public Meter.Id map(Meter.Id id) {
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isEmpty()) {
          List<Tag> tags = new ArrayList<>(id.getTags());
          tags.add(Tag.of("traceId", traceId));
          return id.withTags(tags);
        }
        return id;
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

