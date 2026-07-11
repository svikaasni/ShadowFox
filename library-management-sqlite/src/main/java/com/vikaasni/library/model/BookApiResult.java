package com.vikaasni.library.model;

import java.util.List;

public record BookApiResult(
        String isbn,
        String title,
        List<String> authors,
        String genre,
        String description,
        String publishedDate
) {}
