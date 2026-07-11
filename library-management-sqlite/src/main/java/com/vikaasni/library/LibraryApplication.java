package com.vikaasni.library;

import com.vikaasni.library.api.GoogleBooksClient;
import com.vikaasni.library.config.DatabaseManager;
import com.vikaasni.library.dao.BookDao;
import com.vikaasni.library.dao.LoanDao;
import com.vikaasni.library.dao.UserDao;
import com.vikaasni.library.model.Book;
import com.vikaasni.library.model.BookApiResult;
import com.vikaasni.library.model.Loan;
import com.vikaasni.library.model.User;
import com.vikaasni.library.service.LibraryService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class LibraryApplication {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final BookDao bookDao = new BookDao();
    private final LibraryService libraryService = new LibraryService(bookDao, new LoanDao());
    private final GoogleBooksClient booksClient = new GoogleBooksClient();

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        LibraryApplication app = new LibraryApplication();
        app.seedAdmin();
        
        try {
            int port = 8080;
            com.vikaasni.library.web.WebServer webServer = new com.vikaasni.library.web.WebServer(port);
            webServer.start();
            System.out.println("=================================================");
            System.out.println("Press ENTER in this console to shut down the server.");
            System.out.println("=================================================");
            new Scanner(System.in).nextLine();
            webServer.stop();
            System.out.println("Server stopped. Goodbye!");
        } catch (Exception e) {
            System.err.println("Could not start Web Server: " + e.getMessage());
            System.out.println("Falling back to CLI Console mode...");
            app.run();
        }
    }

    private void run() {
        seedAdmin();
        System.out.println("\n=== LIBRARY MANAGEMENT SYSTEM ===");
        while (true) {
            System.out.println("""
                    
                    1. Login
                    2. Register member
                    0. Exit""");
            switch (readInt("Choose: ")) {
                case 1 -> login();
                case 2 -> registerMember();
                case 0 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    private void seedAdmin() {
        if (!userDao.hasAnyUsers()) {
            userDao.register("admin", "Admin@123", "System Administrator", "ADMIN", "Technology");
            System.out.println("Default admin created: admin / Admin@123 (change for real deployment)");
        }
    }

    private void registerMember() {
        try {
            String username = read("Username: ");
            String password = read("Password: ");
            String name = read("Full name: ");
            String genre = read("Favourite genre: ");
            userDao.register(username,password,name,"MEMBER",genre);
            System.out.println("Registration successful");
        } catch (RuntimeException e) { System.out.println(e.getMessage()); }
    }

    private void login() {
        Optional<User> result = userDao.authenticate(read("Username: "), read("Password: "));
        if (result.isEmpty()) { System.out.println("Invalid username or password"); return; }
        User user = result.get();
        if ("ADMIN".equals(user.role())) adminMenu(user); else memberMenu(user);
    }

    private void adminMenu(User user) {
        while (true) {
            System.out.println("\nADMIN: " + user.fullName());
            System.out.println("""
                    1. List books
                    2. Search books
                    3. Add book manually
                    4. Add book using ISBN API
                    0. Logout""");
            try {
                switch (readInt("Choose: ")) {
                    case 1 -> printBooks(bookDao.findAll());
                    case 2 -> printBooks(bookDao.search(read("Keyword: ")));
                    case 3 -> addManualBook();
                    case 4 -> addFromApi();
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice");
                }
            } catch (RuntimeException e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private void memberMenu(User user) {
        while (true) {
            System.out.println("\nMEMBER: " + user.fullName());
            System.out.println("""
                    1. List books
                    2. Search books
                    3. Issue book
                    4. Return book
                    5. My loans
                    6. Recommendations
                    0. Logout""");
            try {
                switch (readInt("Choose: ")) {
                    case 1 -> printBooks(bookDao.findAll());
                    case 2 -> printBooks(bookDao.search(read("Keyword: ")));
                    case 3 -> {
                        long id = readLong("Book ID: ");
                        long loanId = libraryService.issueBook(user.id(), id, LocalDate.now());
                        System.out.println("Issued successfully. Loan ID: " + loanId + ", due: " + LocalDate.now().plusDays(LibraryService.LOAN_DAYS));
                    }
                    case 4 -> {
                        long loanId = readLong("Loan ID: ");
                        double fine = libraryService.returnBook(loanId, LocalDate.now());
                        System.out.printf("Returned successfully. Fine: ₹%.2f%n", fine);
                    }
                    case 5 -> printLoans(libraryService.getUserLoans(user.id()));
                    case 6 -> printBooks(libraryService.recommendations(user.id()));
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice");
                }
            } catch (RuntimeException e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private void addManualBook() {
        String isbn = read("ISBN (optional): ");
        String title = read("Title: ");
        List<String> authors = Arrays.stream(read("Authors, comma separated: ").split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        String genre = read("Genre: ");
        String description = read("Description: ");
        String published = read("Published date/year: ");
        int copies = readInt("Copies: ");
        System.out.println("Added: " + bookDao.addBook(new BookApiResult(isbn,title,authors,genre,description,published),copies));
    }

    private void addFromApi() {
        String isbn = read("ISBN-10 or ISBN-13: ");
        BookApiResult data = booksClient.fetchByIsbn(isbn).orElseThrow(() -> new IllegalArgumentException("No book found for that ISBN"));
        System.out.println("Fetched: " + data.title() + " by " + String.join(", ", data.authors()));
        int copies = readInt("Number of copies to save: ");
        System.out.println("Added: " + bookDao.addBook(data,copies));
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) System.out.println("No books found"); else books.forEach(System.out::println);
    }

    private void printLoans(List<Loan> loans) {
        if (loans.isEmpty()) { System.out.println("No loans found"); return; }
        for (Loan l : loans) System.out.printf("Loan %d | %s | issued %s | due %s | returned %s | fine ₹%.2f | %s%n",
                l.id(),l.bookTitle(),l.issueDate(),l.dueDate(),l.returnDate(),l.fine(),l.status());
    }

    private String read(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    private int readInt(String prompt) { return Integer.parseInt(read(prompt)); }
    private long readLong(String prompt) { return Long.parseLong(read(prompt)); }
}
