package com.learn.csAi.service;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class HealthCareDocsLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCareDocsLoaderService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    @Value("classpath:/healthCare/*.pdf")
    private Resource[] pdfResources;

    @Autowired
    public HealthCareDocsLoaderService(@Qualifier("customVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() {
        loadDocs().subscribe(
                null,
                error -> logger.error("Failed to load healthcare documents", error),
                () -> logger.info("Healthcare documents loaded successfully")
        );
    }

    public Mono<Void> loadDocs() {
        if (pdfResources == null || pdfResources.length == 0) {
            logger.warn("No PDF resources found in classpath:/healthCare/");
            return Mono.just(Collections.emptyList()).then();
        }
        return Mono.fromCallable(() -> Arrays.stream(pdfResources)
                        .map(resource -> {
                            logger.debug("Processing PDF: {}", resource.getFilename());
                            try (InputStream is = resource.getInputStream();
                                 PDDocument pdf = PDDocument.load(is)) {
                                return new PDFTextStripper().getText(pdf);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse PDF: " + resource.getFilename(), e);
                            }
                        })
                        .map(Document::new)
                        .toList())
                .map(textSplitter::split)
                .doOnNext(vectorStore::accept)
                .then();
    }
}
