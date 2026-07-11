package com.vikaasni.library.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private static String databaseUrl = "jdbc:sqlite:" + Path.of("data", "library.db").toAbsolutePath();

    private DatabaseManager() {}

    public static void setDatabaseUrl(String url) {
        databaseUrl = url;
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        if (databaseUrl.startsWith("jdbc:sqlite:") && !databaseUrl.contains(":memory:")) {
            String fileName = databaseUrl.substring("jdbc:sqlite:".length());
            try {
                Path parent = Path.of(fileName).toAbsolutePath().getParent();
                if (parent != null) Files.createDirectories(parent);
            } catch (Exception e) {
                throw new IllegalStateException("Could not create database directory", e);
            }
        }
        String[] statements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                password_salt TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('ADMIN','MEMBER')),
                favorite_genre TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS authors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                isbn TEXT UNIQUE,
                title TEXT NOT NULL,
                genre TEXT,
                description TEXT,
                published_date TEXT,
                total_copies INTEGER NOT NULL CHECK(total_copies >= 0),
                available_copies INTEGER NOT NULL CHECK(available_copies >= 0),
                CHECK(available_copies <= total_copies)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS book_authors (
                book_id INTEGER NOT NULL,
                author_id INTEGER NOT NULL,
                PRIMARY KEY(book_id, author_id),
                FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE CASCADE,
                FOREIGN KEY(author_id) REFERENCES authors(id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS loans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                book_id INTEGER NOT NULL,
                issue_date TEXT NOT NULL,
                due_date TEXT NOT NULL,
                return_date TEXT,
                fine REAL NOT NULL DEFAULT 0,
                status TEXT NOT NULL CHECK(status IN ('ISSUED','RETURNED')),
                FOREIGN KEY(user_id) REFERENCES users(id),
                FOREIGN KEY(book_id) REFERENCES books(id)
            )
            """
        };

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize SQLite database", e);
        }
    }
}
