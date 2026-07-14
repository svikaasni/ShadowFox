package com.vikaasni.bankapp;

import com.vikaasni.bankapp.exception.AccountNotFoundException;
import com.vikaasni.bankapp.exception.InsufficientFundsException;
import com.vikaasni.bankapp.exception.InvalidAmountException;
import com.vikaasni.bankapp.model.BankAccount;
import com.vikaasni.bankapp.model.Transaction;
import com.vikaasni.bankapp.repository.FileAccountRepository;
import com.vikaasni.bankapp.service.BankService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        BankService bankService = new BankService(
                new FileAccountRepository(Path.of("data", "accounts.txt"))
        );

        boolean running = true;

        while (running) {
            printMenu();

            try {
                int choice = Integer.parseInt(SCANNER.nextLine().trim());

                switch (choice) {
                    case 1 -> createAccount(bankService);
                    case 2 -> deposit(bankService);
                    case 3 -> withdraw(bankService);
                    case 4 -> showBalance(bankService);
                    case 5 -> showTransactionHistory(bankService);
                    case 6 -> showAllAccounts(bankService);
                    case 0 -> {
                        running = false;
                        System.out.println("Thank you for using the Bank Account System.");
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid number.");
            } catch (
                    IllegalArgumentException |
                    InvalidAmountException |
                    InsufficientFundsException |
                    AccountNotFoundException exception
            ) {
                System.out.println("Error: " + exception.getMessage());
            }

            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("       BANK ACCOUNT MANAGEMENT");
        System.out.println("========================================");
        System.out.println("1. Create account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Balance inquiry");
        System.out.println("5. Transaction history");
        System.out.println("6. View all accounts");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void createAccount(BankService service) {
        System.out.print("Enter account number: ");
        String accountNumber = SCANNER.nextLine();

        System.out.print("Enter account holder name: ");
        String holderName = SCANNER.nextLine();

        System.out.print("Enter opening balance: ");
        BigDecimal openingBalance = readAmount();

        BankAccount account = service.createAccount(
                accountNumber,
                holderName,
                openingBalance
        );

        System.out.println(
                "Account created successfully for " +
                account.getAccountHolderName()
        );
    }

    private static void deposit(BankService service) {
        String accountNumber = readAccountNumber();

        System.out.print("Enter deposit amount: ");
        BigDecimal amount = readAmount();

        service.deposit(accountNumber, amount);
        System.out.println(
                "Deposit successful. New balance: Rs. " +
                service.getBalance(accountNumber)
        );
    }

    private static void withdraw(BankService service) {
        String accountNumber = readAccountNumber();

        System.out.print("Enter withdrawal amount: ");
        BigDecimal amount = readAmount();

        service.withdraw(accountNumber, amount);
        System.out.println(
                "Withdrawal successful. New balance: Rs. " +
                service.getBalance(accountNumber)
        );
    }

    private static void showBalance(BankService service) {
        String accountNumber = readAccountNumber();
        System.out.println(
                "Current balance: Rs. " +
                service.getBalance(accountNumber)
        );
    }

    private static void showTransactionHistory(BankService service) {
        String accountNumber = readAccountNumber();
        List<Transaction> history = service.getTransactionHistory(accountNumber);

        System.out.println("\n----------- MINI STATEMENT -----------");

        if (history.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            history.forEach(System.out::println);
        }

        System.out.println("--------------------------------------");
    }

    private static void showAllAccounts(BankService service) {
        List<BankAccount> accounts = service.getAllAccounts();

        if (accounts.isEmpty()) {
            System.out.println("No accounts available.");
            return;
        }

        System.out.printf("%-15s %-25s %-12s%n",
                "Account No.", "Holder Name", "Balance");
        System.out.println("------------------------------------------------------");

        for (BankAccount account : accounts) {
            System.out.printf("%-15s %-25s Rs. %-10s%n",
                    account.getAccountNumber(),
                    account.getAccountHolderName(),
                    account.getBalance().toPlainString());
        }
    }

    private static String readAccountNumber() {
        System.out.print("Enter account number: ");
        return SCANNER.nextLine().trim();
    }

    private static BigDecimal readAmount() {
        return new BigDecimal(SCANNER.nextLine().trim());
    }
}
