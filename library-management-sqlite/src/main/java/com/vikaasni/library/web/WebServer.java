package com.vikaasni.library.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vikaasni.library.api.GoogleBooksClient;
import com.vikaasni.library.dao.BookDao;
import com.vikaasni.library.dao.LoanDao;
import com.vikaasni.library.dao.UserDao;
import com.vikaasni.library.model.Book;
import com.vikaasni.library.model.BookApiResult;
import com.vikaasni.library.model.Loan;
import com.vikaasni.library.model.User;
import com.vikaasni.library.service.LibraryService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public final class WebServer {
    private final HttpServer server;
    private final UserDao userDao = new UserDao();
    private final BookDao bookDao = new BookDao();
    private final LoanDao loanDao = new LoanDao();
    private final LibraryService libraryService = new LibraryService(bookDao, loanDao);
    private final GoogleBooksClient booksClient = new GoogleBooksClient();

    public WebServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupContexts();
    }

    public void start() {
        server.start();
        System.out.println("Web UI Server started on port " + server.getAddress().getPort());
        System.out.println("Visit http://localhost:" + server.getAddress().getPort() + " to access the library");
    }

    public void stop() {
        server.stop(0);
    }

    private void setupContexts() {
        server.createContext("/", new StaticHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/books", new BooksHandler());
        server.createContext("/api/books/recommendations", new RecommendationsHandler());
        server.createContext("/api/books/add", new AddBookHandler());
        server.createContext("/api/loans", new LoansHandler());
        server.createContext("/api/loans/issue", new IssueLoanHandler());
        server.createContext("/api/loans/return", new ReturnLoanHandler());
    }

    // Static Files Handler
    private static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }
            
            InputStream is = WebServer.class.getResourceAsStream("/web" + path);
            if (is == null) {
                // Fallback for development run
                File file = new File("src/main/resources/web" + path);
                if (file.exists()) {
                    is = new FileInputStream(file);
                }
            }

            if (is == null) {
                String error = "404 Not Found";
                exchange.sendResponseHeaders(404, error.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
                return;
            }

            byte[] bytes = is.readAllBytes();
            is.close();

            String contentType = "text/html";
            if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // Auth Handlers
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();

                Optional<User> result = userDao.authenticate(username, password);
                if (result.isPresent()) {
                    sendJson(exchange, 200, result.get());
                } else {
                    Map<String, String> err = Map.of("message", "Invalid username or password");
                    sendJson(exchange, 401, err);
                }
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();
                String fullName = json.get("fullName").getAsString();
                String favoriteGenre = json.get("favoriteGenre").getAsString();

                User user = userDao.register(username, password, fullName, "MEMBER", favoriteGenre);
                sendJson(exchange, 200, user);
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    // Books Handler
    private class BooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                String searchKeyword = null;
                if (query != null && query.contains("q=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("q=")) {
                            searchKeyword = java.net.URLDecoder.decode(param.substring(2), StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }

                List<Book> books;
                if (searchKeyword != null && !searchKeyword.isBlank()) {
                    books = bookDao.search(searchKeyword);
                } else {
                    books = bookDao.findAll();
                }
                sendJson(exchange, 200, books);
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    private class RecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                long userId = -1;
                if (query != null && query.contains("userId=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("userId=")) {
                            userId = Long.parseLong(param.substring("userId=".length()));
                            break;
                        }
                    }
                }
                if (userId == -1) {
                    sendJson(exchange, 400, Map.of("message", "userId is required"));
                    return;
                }
                List<Book> recommendations = libraryService.recommendations(userId);
                sendJson(exchange, 200, recommendations);
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    // Ingest Book
    private class AddBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String method = json.get("method").getAsString();
                int copies = json.get("totalCopies").getAsInt();

                BookApiResult resultData;
                if ("ISBN".equalsIgnoreCase(method)) {
                    String isbn = json.get("isbn").getAsString();
                    resultData = booksClient.fetchByIsbn(isbn)
                            .orElseThrow(() -> new IllegalArgumentException("No book metadata found for ISBN " + isbn));
                } else {
                    String title = json.get("title").getAsString();
                    String isbn = json.has("isbn") && !json.get("isbn").isJsonNull() ? json.get("isbn").getAsString() : null;
                    List<String> authors = new ArrayList<>();
                    if (json.has("authors")) {
                        json.getAsJsonArray("authors").forEach(e -> authors.add(e.getAsString()));
                    }
                    String genre = json.has("genre") && !json.get("genre").isJsonNull() ? json.get("genre").getAsString() : null;
                    String desc = json.has("description") && !json.get("description").isJsonNull() ? json.get("description").getAsString() : null;
                    String pubDate = json.has("publishedDate") && !json.get("publishedDate").isJsonNull() ? json.get("publishedDate").getAsString() : null;

                    resultData = new BookApiResult(isbn, title, authors, genre, desc, pubDate);
                }

                Book book = bookDao.addBook(resultData, copies);
                sendJson(exchange, 200, book);
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    // Loan handlers
    private class LoansHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                long userId = -1;
                if (query != null && query.contains("userId=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("userId=")) {
                            userId = Long.parseLong(param.substring("userId=".length()));
                            break;
                        }
                    }
                }
                if (userId == -1) {
                    sendJson(exchange, 400, Map.of("message", "userId is required"));
                    return;
                }
                List<Loan> loans = libraryService.getUserLoans(userId);
                sendJson(exchange, 200, loans);
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    private class IssueLoanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                long userId = json.get("userId").getAsLong();
                long bookId = json.get("bookId").getAsLong();

                long loanId = libraryService.issueBook(userId, bookId, LocalDate.now());
                sendJson(exchange, 200, Map.of("loanId", loanId));
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    private class ReturnLoanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                long loanId = json.get("loanId").getAsLong();

                double fine = libraryService.returnBook(loanId, LocalDate.now());
                sendJson(exchange, 200, Map.of("fine", fine));
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        }
    }

    // Helper utilities
    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        byte[] bytes = new Gson().toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 400, Map.of("message", message != null ? message : "An unknown error occurred"));
    }
}
