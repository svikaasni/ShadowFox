package com.vikaasni.bankapp.model;

import com.vikaasni.bankapp.exception.InsufficientFundsException;
import com.vikaasni.bankapp.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BankAccount {
    private final String accountNumber;
    private final String accountHolderName;
    private BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public BankAccount(String accountNumber, String accountHolderName) {
        this(accountNumber, accountHolderName, BigDecimal.ZERO);
    }

    public BankAccount(
            String accountNumber,
            String accountHolderName,
            BigDecimal openingBalance
    ) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be blank.");
        }
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new IllegalArgumentException("Account holder name cannot be blank.");
        }

        BigDecimal normalizedOpeningBalance = normalize(openingBalance);
        if (normalizedOpeningBalance.signum() < 0) {
            throw new InvalidAmountException("Opening balance cannot be negative.");
        }

        this.accountNumber = accountNumber.trim();
        this.accountHolderName = accountHolderName.trim();
        this.balance = normalizedOpeningBalance;

        if (normalizedOpeningBalance.signum() > 0) {
            transactions.add(new Transaction(
                    TransactionType.DEPOSIT,
                    normalizedOpeningBalance,
                    normalizedOpeningBalance,
                    LocalDateTime.now()
            ));
        }
    }

    public synchronized void deposit(BigDecimal amount) {
        BigDecimal normalizedAmount = validatePositiveAmount(amount);
        balance = balance.add(normalizedAmount);

        transactions.add(new Transaction(
                TransactionType.DEPOSIT,
                normalizedAmount,
                balance,
                LocalDateTime.now()
        ));
    }

    public synchronized void withdraw(BigDecimal amount) {
        BigDecimal normalizedAmount = validatePositiveAmount(amount);

        if (normalizedAmount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available balance: Rs. " + balance.toPlainString()
            );
        }

        balance = balance.subtract(normalizedAmount);

        transactions.add(new Transaction(
                TransactionType.WITHDRAWAL,
                normalizedAmount,
                balance,
                LocalDateTime.now()
        ));
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public synchronized List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(new ArrayList<>(transactions));
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount) {
        BigDecimal normalizedAmount = normalize(amount);
        if (normalizedAmount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        return normalizedAmount;
    }

    private static BigDecimal normalize(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null.");
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
