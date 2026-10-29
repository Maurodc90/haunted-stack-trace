package com.maurodc90.hauntedstacktrace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * The ghost's answer to a stack trace.
 * The factual fields (exception, culprit, cause, fix) stay plain so a developer can act on them;
 * only the whisper is in character.
 *
 * @param ghost     the deprecated Java API that speaks
 * @param exception the exception class from the trace
 * @param culprit   the first frame of the application's own code
 * @param cause     plain-English reason the exception happened
 * @param fix       concrete way to fix it
 * @param whisper   short in-character line from the ghost
 */
public record Haunting(
        @JsonProperty(required = true)
        @JsonPropertyDescription("One of: java.util.Date, Vector, Hashtable, Applet, StringTokenizer")
        String ghost,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Fully qualified exception class, copied from the trace")
        String exception,

        @JsonProperty(required = true)
        @JsonPropertyDescription("First frame from the application's own code, copied exactly, e.g. com.example.Foo.bar(Foo.java:42)")
        String culprit,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Why it happened, one or two plain sentences, not in character")
        String cause,

        @JsonProperty(required = true)
        @JsonPropertyDescription("How to fix it, practical and specific, not in character")
        String fix,

        @JsonProperty(required = true)
        @JsonPropertyDescription("One or two eerie in-character sentences, under 40 words")
        String whisper) {
}