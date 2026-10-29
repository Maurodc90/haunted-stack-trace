package com.maurodc90.hauntedstacktrace.controller;

import com.maurodc90.hauntedstacktrace.model.Haunting;
import com.maurodc90.hauntedstacktrace.service.GhostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/**
 * Accepts a pasted stack trace from any Java project and returns the ghost's explanation.
 */
@RestController
public class SeanceController {

    // A stack frame line looks like "at com.example.Foo.bar(Foo.java:42)".
    private static final Pattern STACK_FRAME = Pattern.compile("^\\s*at \\S+\\(", Pattern.MULTILINE);

    private final GhostService ghostService;

    /**
     * @param ghostService service that asks the model for an explanation
     */
    public SeanceController(GhostService ghostService) {
        this.ghostService = ghostService;
    }

    /**
     * Explains a stack trace sent as plain text.
     * Text without a single stack frame is rejected before it reaches the model.
     *
     * @param stackTrace the raw stack trace
     * @return 200 with the explanation, or 400 if the text is not a stack trace
     */
    @PostMapping(value = "/seance", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Haunting> seance(@RequestBody String stackTrace) {
        if (!STACK_FRAME.matcher(stackTrace).find()) {
            Haunting refusal = new Haunting(
                    "Applet",
                    "none",
                    "none",
                    "The text sent is not a Java stack trace, so there is nothing to explain.",
                    "Paste the full exception output, including the lines that start with 'at'.",
                    "You knock, but bring no bones. The dead answer only to stack traces.");
            return ResponseEntity.badRequest().body(refusal);
        }
        return ResponseEntity.ok(ghostService.summon(stackTrace));
    }
}