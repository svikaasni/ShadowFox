package com.vikaasni.library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class FineService {
    private FineService() {}

    public static double calculateFine(LocalDate dueDate, LocalDate returnDate, double dailyFine) {
        if (dueDate == null || returnDate == null) throw new IllegalArgumentException("Dates cannot be null");
        if (dailyFine < 0) throw new IllegalArgumentException("Daily fine cannot be negative");
        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        return overdueDays * dailyFine;
    }
}
