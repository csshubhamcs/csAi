package com.learn.csAi.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class HealthCareDocsLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCareDocsLoaderService.class);
    private final VectorStore vectorStore; // Where we store the document chunks
    private final PathMatchingResourcePatternResolver resolver; // Helps find PDF files
    private final ExecutorService executorService; // Runs tasks in parallel

    @Autowired // Spring sets up the VectorStore for us
    public HealthCareDocsLoaderService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.resolver = new PathMatchingResourcePatternResolver();
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }


    @PostConstruct // Runs this method when the app starts
    public void init() {
        try {
            // Find all PDFs in the healthCare folder
            Resource[] pdfFiles = resolver.getResources("classpath:/healthCare/*.pdf");
            if (pdfFiles.length == 0) {
                logger.warn("No PDFs found in classpath:/healthCare/");
                return; // Exit if no files are found
            }

            // Process PDFs in parallel
            List<Future<List<Document>>> tasks = new ArrayList<>();
            for (Resource pdf : pdfFiles) {
                tasks.add(executorService.submit(() -> processPdf(pdf)));
            }

            // Collect all the chunks from the processed PDFs
            List<Document> allChunks = new ArrayList<>();
            for (Future<List<Document>> task : tasks) {
                allChunks.addAll(task.get()); // Wait for each task to finish and add its results
            }

            // Save all chunks to the VectorStore
            vectorStore.accept(allChunks);
            logger.info("All healthcare PDFs loaded successfully!");
        } catch (Exception e) {
            logger.error("Error loading PDFs", e);
            throw new RuntimeException("Could not load healthcare PDFs", e); // Stop the app if something goes wrong
        } finally {
            executorService.shutdown(); // Clean up the parallel processor
        }
    }

    private List<Document> processPdf(Resource pdf) {
        try {
            logger.debug("Processing PDF: {}", pdf.getFilename());
            try (InputStream is = pdf.getInputStream();
                 PDDocument pdfDoc = PDDocument.load(is)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String content = stripper.getText(pdfDoc);
                TextSplitter splitter = new TokenTextSplitter();
                List<Document> chunks = splitter.split(new Document(content));
                logger.info("Split {} into {} chunks", pdf.getFilename(), chunks.size());
                return chunks;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF: " + pdf.getFilename(), e);
        }
    }
}
