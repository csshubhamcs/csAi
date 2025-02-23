package com.learn.csAi.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.ai.vectorstore.SearchRequest.DEFAULT_TOP_K;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired(required = false)
    @Qualifier("customOpenAiChatClientBuilder")
    private ChatClient.Builder openAiBuilder;

    @Autowired(required = false)
    @Qualifier("customOllamaChatClientBuilder")
    private ChatClient.Builder ollamaBuilder;

    private final VectorStore vectorStore;
    private final DslExamplesLoader dslExamplesLoader;
    private final ChatMemory chatMemory;


    private Map<String, ChatClient.Builder> chatClientBuilders;

    @Autowired
    public ChatService(@Qualifier("customVectorStore") VectorStore vectorStore,
                       DslExamplesLoader dslExamplesLoader,
                       @Qualifier("chatMemory") ChatMemory chatMemory) {
        this.vectorStore = vectorStore;
        this.dslExamplesLoader = dslExamplesLoader;
        this.chatMemory = chatMemory;
    }


    @PostConstruct
    public void init() {
        chatClientBuilders = new HashMap<>();
        if (openAiBuilder != null) {
            chatClientBuilders.put("openai", openAiBuilder);
            logger.info("OpenAI provider initialized");
        }
        if (ollamaBuilder != null) {
            chatClientBuilders.put("ollama", ollamaBuilder);
            logger.info("Ollama provider initialized");
        }
        Assert.notEmpty(chatClientBuilders, "No chat providers configured. Please check application.properties.");
    }






    public Flux<String> getDslCode(String provider, String description) {
        logger.debug("Processing DSL request: provider={}, description={}", provider, description);
        return Optional.ofNullable(chatClientBuilders.get(provider))
                .map(builder -> dslExamplesLoader.loadExamples()
                        .map(this::buildDslSystemPrompt)
                        .map(prompt -> builder.build().prompt().system(prompt).user(description))
                        .flatMapMany(prompt -> prompt.stream().content()))
                .orElseGet(() -> {
                    logger.warn("Invalid provider: {}", provider);
                    return Flux.error(new IllegalArgumentException("Invalid provider: " + provider));
                });
    }

    public Flux<String> getHealthCareAnswer(String provider, String question) {
        logger.debug("Processing healthcare request: provider={}, question={}", provider, question);
        return Optional.ofNullable(chatClientBuilders.get(provider))
                .map(builder -> builder.build()
                        .prompt()
                        .system("You are a healthcare expert.")
                        .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().topK(DEFAULT_TOP_K).similarityThresholdAll().build()))
                        .user(question)
                        .stream()
                        .content())
                .map(Flux::from)
                .orElseGet(() -> {
                    logger.warn("Provider not available: {}", provider);
                    return Flux.error(new IllegalArgumentException("Provider not available: " + provider));
                });

    }

    private String buildDslSystemPrompt(List<Map<String, String>> examples) {
        if (examples == null || examples.isEmpty()) {
            return "You are an expert in converting descriptions to code. No examples provided.";
        }
        return examples.stream()
                .map(e -> "Description: '%s' -> Code: '%s'".formatted(e.get("description"), e.get("code")))
                .collect(Collectors.joining("\n", "You are an expert in converting descriptions to code. Examples:\n", "\nNow, convert the following description to code:"));
    }
}
