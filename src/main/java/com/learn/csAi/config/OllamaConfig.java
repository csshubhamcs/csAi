package com.learn.csAi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    private final OllamaChatModel ollamaChatModel;

    @Autowired
    public OllamaConfig(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Bean("customOllamaChatClientBuilder")
    public ChatClient.Builder ollamaChatClientBuilder(ChatMemory chatMemory) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory));
    }

    @Bean("customOllamaChatClient")
    public ChatClient ollamaChatClient(ChatMemory chatMemory) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultSystem("You are a helpful AI Assistant.")
                .build();
    }
}
