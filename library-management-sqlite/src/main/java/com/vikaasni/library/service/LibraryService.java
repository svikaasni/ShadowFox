package com.vikaasni.library.service;

import com.vikaasni.library.dao.BookDao;
import com.vikaasni.library.dao.LoanDao;
import com.vikaasni.library.model.Book;
import com.vikaasni.library.model.Loan;

import java.time.LocalDate;
import java.util.List;

public class LibraryService {
    public static final int LOAN_DAYS = 14;
    public static final double DAILY_FINE = 5.0;
    private final BookDao bookDao;
    private final LoanDao loanDao;

    public LibraryService(BookDao bookDao, LoanDao loanDao) {
        this.bookDao = bookDao;
        this.loanDao = loanDao;
    }

    public long issueBook(long userId, long bookId, LocalDate issueDate) {
        if (loanDao.findActiveLoan(userId, bookId).isPresent()) {
            throw new IllegalStateException("This user already has this book issued");
        }
        Book book = bookDao.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (book.availableCopies() <= 0) throw new IllegalStateException("No copy is available");
        return loanDao.createLoan(userId, bookId, issueDate, issueDate.plusDays(LOAN_DAYS));
    }

    public double returnBook(long loanId, LocalDate returnDate) {
        return loanDao.returnLoan(loanId, returnDate, DAILY_FINE);
    }

    public List<Loan> getUserLoans(long userId) { return loanDao.findByUser(userId); }
    public List<Book> recommendations(long userId) { return bookDao.recommendForUser(userId); }
}
