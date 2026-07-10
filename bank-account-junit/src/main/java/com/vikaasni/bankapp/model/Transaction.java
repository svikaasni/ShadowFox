package com.vikaasni.bankapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class Transaction {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfterTransaction;
    private final LocalDateTime timestamp;

    public Transaction(
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfterTransaction,
            LocalDateTime timestamp
    ) {
        this.type = Objects.requireNonNull(type);
        this.amount = Objects.requireNonNull(amount);
        this.balanceAfterTransaction = Objects.requireNonNull(balanceAfterTransaction);
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "%s | %-10s | Amount: Rs. %s | Balance: Rs. %s".formatted(
                timestamp.format(FORMATTER),
                type,
                amount.toPlainString(),
                balanceAfterTransaction.toPlainString()
        );
    }
}
