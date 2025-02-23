package com.learn.csAi.controller;

import com.learn.csAi.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequestMapping("/api")
public class ChatController {

    private static final String DEFAULT_EXPERT = "healthcare";
    private static final String DEFAULT_PROVIDER = "ollama";
    private static final String OPENAI_PROVIDER = "openai";

    private final ChatService chatService;
    private final String openAiApiKey;

    @Autowired
    public ChatController(ChatService chatService,
                          @Value("${spring.ai.openai.api-key:#{null}}") String openAiApiKey) {
        this.chatService = chatService;
        this.openAiApiKey = openAiApiKey;
    }

    @GetMapping({"/chat", "/chat/{expertType}"})
    public Flux<String> chat(@PathVariable(required = false) String expertType,
                             @RequestParam String message,
                             @RequestParam(required = false) String provider) {
        if (!StringUtils.hasText(message)) {
            log.warn("Invalid request: message is empty");
            return Flux.error(new IllegalArgumentException("Message must not be empty"));
        }
        log.info("Chat request: expertType={}, message={}, provider={}", expertType, message, provider);


        String effectiveExpertType = StringUtils.hasText(expertType) ? expertType.toLowerCase() : DEFAULT_EXPERT;
        String effectiveProvider = determineProvider(provider);

        log.debug("Chat request: expertType={}, message={}, provider={}", effectiveExpertType, message, effectiveProvider);
        return switch (effectiveExpertType) {
            case "dsl" -> chatService.getDslCode(effectiveProvider, message);
            case "healthcare" -> chatService.getHealthCareAnswer(effectiveProvider, message);
            default -> {
                log.warn("Invalid expert type: {}", effectiveExpertType);
                yield Flux.error(new IllegalArgumentException("Invalid expert type: " + effectiveExpertType));
            }
        };
    }


    private String determineProvider(String providedProvider) {
        if (StringUtils.hasText(providedProvider)) {
            return providedProvider.toLowerCase();
        }
        return StringUtils.hasText(openAiApiKey) ? OPENAI_PROVIDER : DEFAULT_PROVIDER;
    }
}
