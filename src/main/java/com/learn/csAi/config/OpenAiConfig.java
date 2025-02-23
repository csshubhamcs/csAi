package com.learn.csAi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OpenAiConfig {

    private final OpenAiChatModel openAiChatModel;

    @Autowired
    public OpenAiConfig(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @Bean("customOpenAiChatClientBuilder")
    public ChatClient.Builder openAiChatClientBuilder() {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory()));
    }

    @Bean("customOpenAiChatClient")
    public ChatClient openAiChatClient() {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory()))
                .defaultSystem("You are a helpful AI Assistant.")
                .build();
    }

    @Bean("openAiChatMemory")
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
