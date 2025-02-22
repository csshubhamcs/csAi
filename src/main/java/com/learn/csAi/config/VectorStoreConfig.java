package com.learn.csAi.config;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class VectorStoreConfig {

    private final OpenAiEmbeddingModel embeddingModel;

    @Autowired
    public VectorStoreConfig(OpenAiEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Bean("customVectorStore") // Renamed to avoid conflict
    @Primary
    public VectorStore vectorStore(MongoTemplate mongoTemplate) {
        return MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)
                .build();
    }
}
