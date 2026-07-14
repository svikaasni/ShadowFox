package com.vikaasni.library.service;

import com.vikaasni.library.api.GoogleBooksClient;
import com.vikaasni.library.model.BookApiResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GoogleBooksParsingTest {
    @Test void parsesGoogleBooksJson() {
        String json = """
        {"totalItems":1,"items":[{"volumeInfo":{"title":"Clean Code","authors":["Robert C. Martin"],"categories":["Computers"],"publishedDate":"2008","description":"A software craftsmanship book"}}]}
        """;
        BookApiResult result = GoogleBooksClient.parseResponse("9780132350884", json).orElseThrow();
        assertEquals("Clean Code", result.title());
        assertEquals("Robert C. Martin", result.authors().get(0));
        assertEquals("Computers", result.genre());
    }

    @Test void returnsEmptyWhenNoItemsExist() {
        assertTrue(GoogleBooksClient.parseResponse("0000000000", "{\"totalItems\":0}").isEmpty());
    }
}
