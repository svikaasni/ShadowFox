package com.vikaasni.bankapp.repository;

import com.vikaasni.bankapp.model.BankAccount;
import com.vikaasni.bankapp.model.Transaction;
import com.vikaasni.bankapp.model.TransactionType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Simple text-file persistence.
 *
 * Each account is stored as:
 * accountNumber|accountHolderName|balance
 *
 * This is intentionally simple for a student project. The service saves the
 * latest account state after every deposit/withdrawal.
 */
public class FileAccountRepository implements AccountRepository {
    private final Path filePath;
    private final Map<String, BankAccount> accounts = new LinkedHashMap<>();

    public FileAccountRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    @Override
    public synchronized void save(BankAccount account) {
        accounts.put(account.getAccountNumber(), account);
        writeAll();
    }

    @Override
    public synchronized Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    @Override
    public synchronized List<BankAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public synchronized boolean existsByAccountNumber(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    private void load() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length != 3) {
                    continue;
                }

                String accountNumber = unescape(parts[0]);
                String accountHolder = unescape(parts[1]);
                BigDecimal balance = new BigDecimal(parts[2]);

                accounts.put(
                        accountNumber,
                        new BankAccount(accountNumber, accountHolder, balance)
                );
            }
        } catch (IOException | NumberFormatException exception) {
            throw new IllegalStateException("Unable to load account data.", exception);
        }
    }

    private void writeAll() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            )) {
                for (BankAccount account : accounts.values()) {
                    writer.write(
                            escape(account.getAccountNumber()) + "|" +
                            escape(account.getAccountHolderName()) + "|" +
                            account.getBalance().toPlainString()
                    );
                    writer.newLine();
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save account data.", exception);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\p");
    }

    private static String unescape(String value) {
        return value.replace("\\p", "|").replace("\\\\", "\\");
    }
}
