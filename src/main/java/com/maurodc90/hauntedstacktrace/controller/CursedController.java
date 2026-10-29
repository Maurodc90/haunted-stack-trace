package com.maurodc90.hauntedstacktrace.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Endpoints that throw real exceptions on purpose so the ghost has something to haunt.
 */
@RestController
@RequestMapping("/cursed")
public class CursedController {

    /**
     * @return never returns; throws NullPointerException
     */
    @GetMapping("/null")
    public String nullSpirit() {
        String spirit = null;
        return "Spirit name length: " + spirit.length();
    }

    /**
     * @return never returns; throws ArrayIndexOutOfBoundsException
     */
    @GetMapping("/index")
    public String fourthCandle() {
        int[] candles = new int[3];
        return "Fourth candle: " + candles[3];
    }

    /**
     * @return never returns; throws NumberFormatException
     */
    @GetMapping("/number")
    public String unluckyNumber() {
        return "Number: " + Integer.parseInt("thirteen");
    }

    /**
     * @return never returns; throws ConcurrentModificationException
     */
    @GetMapping("/concurrent")
    public String removeWhileIterating() {
        List<String> guests = new ArrayList<>(List.of("Vlad", "Mary", "Banquo"));
        for (String guest : guests) {
            if (guest.equals("Vlad")) {
                guests.remove(guest);
            }
        }
        return "Guests left: " + guests;
    }

    /**
     * @return never returns; throws DateTimeParseException, which java.util.Date's ghost enjoys
     */
    @GetMapping("/date")
    public String halloweenDate() {
        return "Halloween: " + LocalDate.parse("31/10/2026");
    }
}