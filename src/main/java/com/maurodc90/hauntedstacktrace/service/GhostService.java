package com.maurodc90.hauntedstacktrace.service;

import com.maurodc90.hauntedstacktrace.model.Haunting;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Sends a stack trace to a local open-weight model and returns the ghost's structured answer.
 */
@Service
public class GhostService {

    private static final String SYSTEM_PROMPT = """
            You are the ghost of a deprecated Java API haunting a Spring Boot application.
            Choose the ghost whose history best fits the exception:
            java.util.Date, Vector, Hashtable, Applet, StringTokenizer.

            You receive a Java stack trace.
            - culprit: the first frame from the application's own code. Skip frames starting with
              java., jdk., sun., org.springframework., org.apache. Copy the frame exactly.
            - cause and fix: accurate, plain, practical English. No role-play in these two fields.
            - whisper: in character, eerie, under 40 words.
            Never invent class names or line numbers that are not in the trace.
            """;

    private final ChatClient chatClient;
    private final String model;
    private final int maxTraceLines;

    /**
     * Builds the service with the auto-configured ChatClient and the ghost settings.
     *
     * @param chatClientBuilder builder provided by the Spring AI Ollama starter
     * @param model             Ollama model name, e.g. gemma4:12b
     * @param maxTraceLines     how many lines of the trace are sent to the model
     */
    public GhostService(ChatClient.Builder chatClientBuilder,
                        @Value("${ghost.model}") String model,
                        @Value("${ghost.max-trace-lines}") int maxTraceLines) {
        this.chatClient = chatClientBuilder.build();
        this.model = model;
        this.maxTraceLines = maxTraceLines;
    }

    /**
     * Asks the ghost to explain a stack trace.
     *
     * @param stackTrace full stack trace text
     * @return the structured explanation
     */
    public Haunting summon(String stackTrace) {
        String trimmedTrace = stackTrace.lines()
                .limit(maxTraceLines)
                .collect(Collectors.joining("\n"));

        // Generates a JSON Schema from the record; Ollama is forced to answer in that shape.
        BeanOutputConverter<Haunting> converter = new BeanOutputConverter<>(Haunting.class);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(trimmedTrace)
                // Spring AI 2.0: options are passed as a builder, not a built object.
                .options(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(0.4) // low enough to stay accurate, high enough for a voice
                        .disableThinking() // Gemma 4 reasons silently by default; that ate the token budget
                        .numPredict(400)) // hard cap on answer length: the model can never write forever
                .call()
                .entity(converter);
    }
}