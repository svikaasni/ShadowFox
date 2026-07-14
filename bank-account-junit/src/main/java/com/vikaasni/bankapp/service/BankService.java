package com.vikaasni.bankapp.service;

import com.vikaasni.bankapp.exception.AccountNotFoundException;
import com.vikaasni.bankapp.model.BankAccount;
import com.vikaasni.bankapp.model.Transaction;
import com.vikaasni.bankapp.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class BankService {
    private final AccountRepository repository;

    public BankService(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public BankAccount createAccount(
            String accountNumber,
            String holderName,
            BigDecimal openingBalance
    ) {
        if (repository.existsByAccountNumber(accountNumber)) {
            throw new IllegalArgumentException(
                    "Account number already exists: " + accountNumber
            );
        }

        BankAccount account = new BankAccount(
                accountNumber,
                holderName,
                openingBalance
        );
        repository.save(account);
        return account;
    }

    public void deposit(String accountNumber, BigDecimal amount) {
        BankAccount account = getAccount(accountNumber);
        account.deposit(amount);
        repository.save(account);
    }

    public void withdraw(String accountNumber, BigDecimal amount) {
        BankAccount account = getAccount(accountNumber);
        account.withdraw(amount);
        repository.save(account);
    }

    public BigDecimal getBalance(String accountNumber) {
        return getAccount(accountNumber).getBalance();
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return getAccount(accountNumber).getTransactionHistory();
    }

    public BankAccount getAccount(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public List<BankAccount> getAllAccounts() {
        return repository.findAll();
    }
}
