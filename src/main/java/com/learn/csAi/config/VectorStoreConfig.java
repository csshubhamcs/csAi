package com.learn.csAi.config;



import org.springframework.ai.embedding.EmbeddingModel;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private final EmbeddingModel embeddingModel;

    public VectorStoreConfig(JdbcTemplate jdbcTemplate,
                             @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

    @Bean("customVectorStore")
    @Primary
    public VectorStore vectorStore() {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .build();
    }
}
