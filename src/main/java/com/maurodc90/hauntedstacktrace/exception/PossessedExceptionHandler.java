package com.maurodc90.hauntedstacktrace.exception;

import com.maurodc90.hauntedstacktrace.model.Haunting;
import com.maurodc90.hauntedstacktrace.service.GhostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Catches every unhandled runtime exception and lets the ghost explain it instead of a plain 500 page.
 */
@RestControllerAdvice
public class PossessedExceptionHandler {

    private final GhostService ghostService;

    /**
     * @param ghostService service that asks the model for an explanation
     */
    public PossessedExceptionHandler(GhostService ghostService) {
        this.ghostService = ghostService;
    }

    /**
     * Turns a runtime exception into a ghost's explanation.
     * RuntimeException, not Exception: a missing favicon (NoResourceFoundException) must not summon the model.
     *
     * @param exception the exception thrown by a controller
     * @return 500 with the ghost's explanation, or a fallback if the model is unreachable
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Haunting> possess(RuntimeException exception) {
        StringWriter trace = new StringWriter();
        exception.printStackTrace(new PrintWriter(trace));

        try {
            Haunting haunting = ghostService.summon(trace.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(haunting);
        } catch (Exception modelFailure) {
            // Ollama is down or returned unusable JSON: the app must still answer.
            Haunting silence = new Haunting(
                    "none",
                    exception.getClass().getName(),
                    "unknown",
                    exception.getMessage(),
                    "The model gave no usable answer. Check that Ollama is running (curl localhost:11434) and try again.",
                    "The spirits are silent tonight.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(silence);
        }
    }
}