package com.vikaasni.library.service;

import com.vikaasni.library.config.DatabaseManager;
import com.vikaasni.library.dao.BookDao;
import com.vikaasni.library.dao.LoanDao;
import com.vikaasni.library.dao.UserDao;
import com.vikaasni.library.model.Book;
import com.vikaasni.library.model.BookApiResult;
import com.vikaasni.library.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseIntegrationTest {
    private UserDao userDao;
    private BookDao bookDao;
    private LoanDao loanDao;
    private LibraryService service;

    @BeforeEach void setUp() {
        DatabaseManager.setDatabaseUrl("jdbc:sqlite::memory:");
        // A shared in-memory DB would require one open connection, so use a temporary file per test instead.
        DatabaseManager.setDatabaseUrl("jdbc:sqlite:target/test-library-" + System.nanoTime() + ".db");
        DatabaseManager.initializeDatabase();
        userDao = new UserDao(); bookDao = new BookDao(); loanDao = new LoanDao();
        service = new LibraryService(bookDao, loanDao);
    }

    @Test void registerAuthenticateIssueAndReturn() {
        User user = userDao.register("vikaasni","StrongPass1","S Vikaasni","MEMBER","Technology");
        assertTrue(userDao.authenticate("vikaasni","StrongPass1").isPresent());
        assertTrue(userDao.authenticate("vikaasni","wrong").isEmpty());

        Book book = bookDao.addBook(new BookApiResult("9780134685991","Effective Java",List.of("Joshua Bloch"),"Technology","Java guide","2018"),1);
        long loanId = service.issueBook(user.id(), book.id(), LocalDate.of(2026,7,1));
        assertEquals(0, bookDao.findById(book.id()).orElseThrow().availableCopies());
        assertThrows(IllegalStateException.class, () -> service.issueBook(user.id(), book.id(), LocalDate.of(2026,7,2)));

        double fine = service.returnBook(loanId, LocalDate.of(2026,7,20));
        assertEquals(25.0, fine);
        assertEquals(1, bookDao.findById(book.id()).orElseThrow().availableCopies());
    }
}
