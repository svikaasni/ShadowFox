package com.vikaasni.library.model;

import java.util.List;

public record Book(
        long id,
        String isbn,
        String title,
        List<String> authors,
        String genre,
        String description,
        String publishedDate,
        int totalCopies,
        int availableCopies
) {
    @Override
    public String toString() {
        return id + " | " + title + " | " + String.join(", ", authors)
                + " | Genre: " + (genre == null ? "N/A" : genre)
                + " | Available: " + availableCopies + "/" + totalCopies;
    }
}
