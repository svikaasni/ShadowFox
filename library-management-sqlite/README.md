# Library Management System with SQLite

A Java 21 Maven console project demonstrating SQLite persistence, JDBC, DAO pattern, prepared statements, user accounts, password hashing, book issue/return, overdue fines, recommendations, Google Books API integration, and JUnit testing.

## Features
- Admin and member accounts
- PBKDF2 password hashing with a unique salt
- Normalized SQLite schema (`users`, `authors`, `books`, `book_authors`, `loans`)
- Add/search/list books
- Import metadata from Google Books using ISBN
- Issue for 14 days and return with ₹5/day overdue fine
- Recommendations based on favorite genre and borrowing history
- JUnit unit and SQLite integration tests

## Requirements
- JDK 21+
- Maven 3.9+

## Run
```bash
mvn clean test
mvn exec:java
```

First start creates `data/library.db` and the default administrator:
- Username: `admin`
- Password: `Admin@123`

Change the default password approach before real deployment.

## Optional Google API key
Simple public volume searches can work without a key, but a key is recommended for quota management.

Windows CMD:
```bat
set GOOGLE_BOOKS_API_KEY=your_key_here
mvn exec:java
```

PowerShell:
```powershell
$env:GOOGLE_BOOKS_API_KEY="your_key_here"
mvn exec:java
```

## Direct API test
Browser/Postman/curl:
```text
GET https://www.googleapis.com/books/v1/volumes?q=isbn:9780132350884&maxResults=1
```
Expected: HTTP 200 and JSON containing `items[0].volumeInfo.title`, `authors`, and other metadata.
