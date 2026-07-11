package com.vikaasni.library.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vikaasni.library.model.BookApiResult;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GoogleBooksClient {
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private final HttpClient client;

    private static final Map<String, BookApiResult> MOCK_BOOKS = Map.of(
        "9780132350884", new BookApiResult("9780132350884", "Clean Code", List.of("Robert C. Martin"), "Computers", "A software craftsmanship handbook on writing clean, reusable, and maintainable code.", "2008"),
        "9780134685991", new BookApiResult("9780134685991", "Effective Java", List.of("Joshua Bloch"), "Technology", "A handbook of best-practice guidance for the Java programming language.", "2018"),
        "9780596009205", new BookApiResult("9780596009205", "Head First Java", List.of("Kathy Sierra", "Bert Bates"), "Technology", "A brain-friendly guide to learning Java programming and object-oriented principles.", "2005"),
        "9780201633610", new BookApiResult("9780201633610", "Design Patterns", List.of("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides"), "Software Engineering", "Elements of Reusable Object-Oriented Software design patterns.", "1994")
    );

    public GoogleBooksClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    GoogleBooksClient(HttpClient client) { this.client = client; }

    public Optional<BookApiResult> fetchByIsbn(String isbn) {
        String cleaned = isbn == null ? "" : isbn.replaceAll("[^0-9Xx]", "");
        if (cleaned.length() != 10 && cleaned.length() != 13) {
            throw new IllegalArgumentException("ISBN must contain 10 or 13 characters");
        }
        String query = URLEncoder.encode("isbn:" + cleaned, StandardCharsets.UTF_8);
        String apiKey = System.getenv("GOOGLE_BOOKS_API_KEY");
        String url = BASE_URL + query + "&maxResults=1" + (apiKey == null || apiKey.isBlank() ? "" : "&key=" + apiKey);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                if (MOCK_BOOKS.containsKey(cleaned)) {
                    System.out.println("[API Fallback] API status code: " + response.statusCode() + ". Returning mock data for ISBN " + cleaned);
                    return Optional.of(MOCK_BOOKS.get(cleaned));
                }
                throw new IllegalStateException("Google Books API returned HTTP " + response.statusCode());
            }
            return parseResponse(cleaned, response.body());
        } catch (IOException e) {
            if (MOCK_BOOKS.containsKey(cleaned)) {
                System.out.println("[API Fallback] Network error. Returning mock data for ISBN " + cleaned);
                return Optional.of(MOCK_BOOKS.get(cleaned));
            }
            throw new IllegalStateException("Network error while contacting Google Books", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Google Books request was interrupted", e);
        }
    }

    public static Optional<BookApiResult> parseResponse(String isbn, String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!root.has("items") || root.getAsJsonArray("items").isEmpty()) return Optional.empty();
        JsonObject info = root.getAsJsonArray("items").get(0).getAsJsonObject().getAsJsonObject("volumeInfo");
        String title = string(info, "title", "Unknown title");
        List<String> authors = new ArrayList<>();
        if (info.has("authors")) {
            JsonArray arr = info.getAsJsonArray("authors");
            arr.forEach(e -> authors.add(e.getAsString()));
        }
        if (authors.isEmpty()) authors.add("Unknown author");
        String genre = null;
        if (info.has("categories") && !info.getAsJsonArray("categories").isEmpty()) {
            genre = info.getAsJsonArray("categories").get(0).getAsString();
        }
        return Optional.of(new BookApiResult(isbn, title, authors, genre,
                string(info,"description",null), string(info,"publishedDate",null)));
    }

    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
    }
}
