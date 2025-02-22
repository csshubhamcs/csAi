package com.learn.csAi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@AutoConfiguration
//@AutoConfigureBefore(name = "org.springframework.ai.ollama.OllamaAutoConfiguration")
public class OllamaConfig {

    private final OllamaChatModel ollamaChatModel;

    @Autowired
    public OllamaConfig(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel; // Auto-configured by Spring AI
    }

    @Bean("customOllamaChatClientBuilder") // Unique name
    public ChatClient.Builder ollamaChatClientBuilder(ChatMemory chatMemory) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory));
    }

    @Bean("customOllamaChatClient") // Unique name
    public ChatClient ollamaChatClient(ChatMemory chatMemory) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultSystem("You are a helpful AI Assistant.")
                .build();
    }
}
