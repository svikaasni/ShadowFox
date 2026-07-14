# Bank Account Management System with JUnit 5

A complete Java Maven project demonstrating:

- Account creation
- Deposit
- Withdrawal
- Balance inquiry
- Transaction history / mini statement
- File-based persistence
- JUnit 5 unit testing
- Mockito repository mocking
- Negative testing
- Thread-safe withdrawals using `synchronized`
- Concurrent withdrawal testing
- JaCoCo test coverage report

## Requirements

- Java 17 or later
- Apache Maven

## Project Structure

```text
src
├── main
│   └── java/com/vikaasni/bankapp
│       ├── Main.java
│       ├── exception
│       ├── model
│       ├── repository
│       └── service
└── test
    └── java/com/vikaasni/bankapp
        ├── model
        └── service
```

## Run All Tests

```bash
mvn clean test
```

## Run the Application

```bash
mvn compile exec:java
```

## Generate Test-Coverage Report

```bash
mvn clean test
```

Then open:

```text
target/site/jacoco/index.html
```

## Important Test Cases

1. Deposit increases balance.
2. Withdrawal decreases balance.
3. Balance inquiry returns the correct value.
4. Excess withdrawal throws `InsufficientFundsException`.
5. Zero or negative amounts throw `InvalidAmountException`.
6. Deposit and withdrawal add entries to transaction history.
7. History cannot be modified externally.
8. Mockito verifies that repository `save()` is called.
9. Two threads cannot withdraw more money than available.

## Concurrency Test Plan

Starting balance: Rs. 1000

Two threads both attempt to withdraw Rs. 700 at the same instant.

Expected result:

- One withdrawal succeeds.
- One withdrawal fails with `InsufficientFundsException`.
- Final balance is Rs. 300.
- The account never reaches a negative balance.

The `deposit`, `withdraw`, `getBalance`, and `getTransactionHistory`
methods are synchronized to protect shared account state.
