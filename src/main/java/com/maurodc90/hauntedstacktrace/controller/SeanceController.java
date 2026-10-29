package com.maurodc90.hauntedstacktrace.controller;

import com.maurodc90.hauntedstacktrace.model.Haunting;
import com.maurodc90.hauntedstacktrace.service.GhostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accepts a pasted stack trace from any Java project and returns the ghost's explanation.
 */
@RestController
public class SeanceController {

    private final GhostService ghostService;

    /**
     * @param ghostService service that asks the model for an explanation
     */
    public SeanceController(GhostService ghostService) {
        this.ghostService = ghostService;
    }

    /**
     * Explains a stack trace sent as plain text.
     *
     * @param stackTrace the raw stack trace
     * @return 200 with the explanation, or 400 if the body is blank
     */
    @PostMapping(value = "/seance", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Haunting> seance(@RequestBody String stackTrace) {
        if (stackTrace.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ghostService.summon(stackTrace));
    }
}