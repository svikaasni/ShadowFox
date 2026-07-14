package com.vikaasni.library.dao;

import com.vikaasni.library.config.DatabaseManager;
import com.vikaasni.library.model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDao {
    public long createLoan(long userId, long bookId, LocalDate issueDate, LocalDate dueDate) {
        String update = "UPDATE books SET available_copies=available_copies-1 WHERE id=? AND available_copies>0";
        String insert = "INSERT INTO loans(user_id,book_id,issue_date,due_date,status) VALUES(?,?,?,?, 'ISSUED')";
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setLong(1, bookId);
                    if (ps.executeUpdate() != 1) throw new IllegalStateException("Book is not available");
                }
                long loanId;
                try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, userId); ps.setLong(2, bookId);
                    ps.setString(3, issueDate.toString()); ps.setString(4, dueDate.toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); loanId = rs.getLong(1); }
                }
                con.commit();
                return loanId;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally { con.setAutoCommit(true); }
        } catch (Exception e) {
            if (e instanceof IllegalStateException ise) throw ise;
            throw new IllegalStateException("Could not issue book", e);
        }
    }

    public double returnLoan(long loanId, LocalDate returnDate, double dailyFine) {
        String select = "SELECT book_id,due_date,status FROM loans WHERE id=?";
        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                long bookId;
                LocalDate dueDate;
                try (PreparedStatement ps = con.prepareStatement(select)) {
                    ps.setLong(1, loanId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Loan not found");
                        if (!"ISSUED".equals(rs.getString("status"))) throw new IllegalStateException("Book already returned");
                        bookId = rs.getLong("book_id");
                        dueDate = LocalDate.parse(rs.getString("due_date"));
                    }
                }
                double fine = com.vikaasni.library.service.FineService.calculateFine(dueDate, returnDate, dailyFine);
                try (PreparedStatement ps = con.prepareStatement("UPDATE loans SET return_date=?,fine=?,status='RETURNED' WHERE id=?")) {
                    ps.setString(1, returnDate.toString()); ps.setDouble(2, fine); ps.setLong(3, loanId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE books SET available_copies=available_copies+1 WHERE id=?")) {
                    ps.setLong(1, bookId); ps.executeUpdate();
                }
                con.commit();
                return fine;
            } catch (Exception e) {
                con.rollback(); throw e;
            } finally { con.setAutoCommit(true); }
        } catch (Exception e) {
            if (e instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Could not return book", e);
        }
    }

    public Optional<Loan> findActiveLoan(long userId, long bookId) {
        String sql = "SELECT l.*,b.title FROM loans l JOIN books b ON b.id=l.book_id WHERE l.user_id=? AND l.book_id=? AND l.status='ISSUED'";
        List<Loan> list = query(sql, ps -> { ps.setLong(1,userId); ps.setLong(2,bookId); });
        return list.stream().findFirst();
    }

    public List<Loan> findByUser(long userId) {
        String sql = "SELECT l.*,b.title FROM loans l JOIN books b ON b.id=l.book_id WHERE l.user_id=? ORDER BY l.id DESC";
        return query(sql, ps -> ps.setLong(1,userId));
    }

    private List<Loan> query(String sql, Binder binder) {
        List<Loan> result = new ArrayList<>();
        try (Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rd = rs.getString("return_date");
                    result.add(new Loan(rs.getLong("id"),rs.getLong("user_id"),rs.getLong("book_id"),rs.getString("title"),
                            LocalDate.parse(rs.getString("issue_date")),LocalDate.parse(rs.getString("due_date")),
                            rd == null ? null : LocalDate.parse(rd),rs.getDouble("fine"),rs.getString("status")));
                }
            }
            return result;
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    @FunctionalInterface private interface Binder { void bind(PreparedStatement ps) throws SQLException; }
}
