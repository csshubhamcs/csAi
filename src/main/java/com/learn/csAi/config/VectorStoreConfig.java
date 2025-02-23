package com.learn.csAi.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

@Configuration
public class VectorStoreConfig {

  //    private final OpenAiEmbeddingModel embeddingModel;
  //
  //    @Autowired
  //    public VectorStoreConfig(OpenAiEmbeddingModel embeddingModel) {
  //        this.embeddingModel = embeddingModel;
  //    }
  //
  //    @Bean("customVectorStore") // Renamed to avoid conflict
  //    @Primary
  //    public VectorStore vectorStore(MongoTemplate mongoTemplate) {
  //        return MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)
  //                .build();
  //    }

  private final JdbcTemplate jdbcTemplate;

  @Autowired(required = false)
  @Qualifier("openAiEmbeddingModel")
  private OpenAiEmbeddingModel openAiEmbeddingModel;

  @Autowired(required = false)
  @Qualifier("ollamaEmbeddingModel")
  private OllamaEmbeddingModel ollamaEmbeddingModel;

  @Value("${spring.ai.openai.api-key:#{null}}")
  private String openAiApiKey;

  public VectorStoreConfig(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Bean("customVectorStore")
  @Primary
  public VectorStore vectorStore() {
    EmbeddingModel embeddingModel = determineEmbeddingModel();
    int dimensions =
        (embeddingModel instanceof OpenAiEmbeddingModel)
            ? 1536
            : 1024; // OpenAI: 1536, Ollama: 1024
    return PgVectorStore.builder(jdbcTemplate, embeddingModel).dimensions(dimensions).build();
  }

  private EmbeddingModel determineEmbeddingModel() {
    if (StringUtils.hasText(openAiApiKey) && openAiEmbeddingModel != null) {
      return openAiEmbeddingModel;
    } else if (ollamaEmbeddingModel != null) {
      return ollamaEmbeddingModel;
    } else {
      throw new IllegalStateException(
          "No embedding model available. Please configure either OpenAI or Ollama.");
    }
  }
}
