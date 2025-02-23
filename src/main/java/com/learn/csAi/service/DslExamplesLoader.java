package com.learn.csAi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DslExamplesLoader {

  private final ObjectMapper objectMapper;

  @Value("classpath:/dsl/dsl-example.json")
  private Resource dslExamplesResource;

  public DslExamplesLoader(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Cacheable("dslExamples")
  public Mono<List<Map<String, String>>> loadExamples() {
    return Mono.fromCallable(
            () ->
                objectMapper.readValue(
                    dslExamplesResource.getInputStream(),
                    new TypeReference<List<Map<String, String>>>() {}))
        .onErrorMap(e -> new RuntimeException("Failed to load DSL examples", e));
  }
}
