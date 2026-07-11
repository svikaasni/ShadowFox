package com.vikaasni.library.model;

import java.time.LocalDate;

public record Loan(
        long id,
        long userId,
        long bookId,
        String bookTitle,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate returnDate,
        double fine,
        String status
) {}
