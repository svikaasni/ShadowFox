package com.vikaasni.library.dao;

import com.vikaasni.library.config.DatabaseManager;
import com.vikaasni.library.model.Book;
import com.vikaasni.library.model.BookApiResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDao {
    public Book addBook(BookApiResult data, int copies) {
        if (copies < 1) throw new IllegalArgumentException("Copies must be at least 1");
        String insertBook = "INSERT INTO books(isbn,title,genre,description,published_date,total_copies,available_copies) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                long bookId;
                try (PreparedStatement ps = con.prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, blankToNull(data.isbn()));
                    ps.setString(2, data.title());
                    ps.setString(3, blankToNull(data.genre()));
                    ps.setString(4, blankToNull(data.description()));
                    ps.setString(5, blankToNull(data.publishedDate()));
                    ps.setInt(6, copies);
                    ps.setInt(7, copies);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Book ID not generated");
                        bookId = rs.getLong(1);
                    }
                }
                for (String author : data.authors()) attachAuthor(con, bookId, author);
                con.commit();
                return findById(bookId).orElseThrow();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not add book. ISBN may already exist.", e);
        }
    }

    private void attachAuthor(Connection con, long bookId, String authorName) throws SQLException {
        long authorId;
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO authors(name) VALUES(?) ON CONFLICT(name) DO NOTHING")) {
            ps.setString(1, authorName.trim());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM authors WHERE name=?")) {
            ps.setString(1, authorName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Author not found");
                authorId = rs.getLong(1);
            }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO book_authors(book_id,author_id) VALUES(?,?)")) {
            ps.setLong(1, bookId);
            ps.setLong(2, authorId);
            ps.executeUpdate();
        }
    }

    public List<Book> findAll() {
        return queryBooks("SELECT * FROM books ORDER BY title", null);
    }

    public List<Book> search(String keyword) {
        String sql = """
            SELECT DISTINCT b.* FROM books b
            LEFT JOIN book_authors ba ON b.id=ba.book_id
            LEFT JOIN authors a ON a.id=ba.author_id
            WHERE LOWER(b.title) LIKE ? OR LOWER(COALESCE(b.genre,'')) LIKE ? OR LOWER(COALESCE(a.name,'')) LIKE ?
            ORDER BY b.title
            """;
        String pattern = "%" + keyword.toLowerCase() + "%";
        return queryBooks(sql, ps -> { ps.setString(1, pattern); ps.setString(2, pattern); ps.setString(3, pattern); });
    }

    public Optional<Book> findById(long id) {
        List<Book> books = queryBooks("SELECT * FROM books WHERE id=?", ps -> ps.setLong(1, id));
        return books.stream().findFirst();
    }

    public List<Book> recommendForUser(long userId) {
        String sql = """
            SELECT DISTINCT b.* FROM books b
            JOIN users u ON u.id=?
            WHERE b.available_copies > 0
              AND (
                  LOWER(COALESCE(b.genre,'')) = LOWER(COALESCE(u.favorite_genre,''))
                  OR b.genre IN (
                      SELECT DISTINCT b2.genre FROM loans l2 JOIN books b2 ON l2.book_id=b2.id
                      WHERE l2.user_id=? AND b2.genre IS NOT NULL
                  )
              )
              AND b.id NOT IN (SELECT book_id FROM loans WHERE user_id=? AND status='ISSUED')
            ORDER BY b.available_copies DESC, b.title
            LIMIT 10
            """;
        return queryBooks(sql, ps -> { ps.setLong(1, userId); ps.setLong(2, userId); ps.setLong(3, userId); });
    }

    private List<Book> queryBooks(String sql, SqlBinder binder) {
        List<Book> books = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    books.add(new Book(id, rs.getString("isbn"), rs.getString("title"), loadAuthors(con, id),
                            rs.getString("genre"), rs.getString("description"), rs.getString("published_date"),
                            rs.getInt("total_copies"), rs.getInt("available_copies")));
                }
            }
            return books;
        } catch (SQLException e) {
            throw new IllegalStateException("Book query failed", e);
        }
    }

    private List<String> loadAuthors(Connection con, long bookId) throws SQLException {
        List<String> authors = new ArrayList<>();
        String sql = "SELECT a.name FROM authors a JOIN book_authors ba ON a.id=ba.author_id WHERE ba.book_id=? ORDER BY a.name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) authors.add(rs.getString(1));
            }
        }
        return authors;
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    @FunctionalInterface private interface SqlBinder { void bind(PreparedStatement ps) throws SQLException; }
}
