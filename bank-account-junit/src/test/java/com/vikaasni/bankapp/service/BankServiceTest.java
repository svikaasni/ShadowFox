package com.vikaasni.bankapp.service;

import com.vikaasni.bankapp.exception.AccountNotFoundException;
import com.vikaasni.bankapp.model.BankAccount;
import com.vikaasni.bankapp.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private AccountRepository repository;

    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankService = new BankService(repository);
    }

    @Test
    @DisplayName("Creating an account should save it in the repository")
    void createAccountShouldSaveAccount() {
        when(repository.existsByAccountNumber("ACC3001"))
                .thenReturn(false);

        BankAccount account = bankService.createAccount(
                "ACC3001",
                "Test User",
                new BigDecimal("500.00")
        );

        assertEquals("ACC3001", account.getAccountNumber());
        assertEquals(new BigDecimal("500.00"), account.getBalance());
        verify(repository).save(account);
    }

    @Test
    @DisplayName("Duplicate account number should be rejected")
    void duplicateAccountNumberShouldBeRejected() {
        when(repository.existsByAccountNumber("ACC3001"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> bankService.createAccount(
                        "ACC3001",
                        "Test User",
                        BigDecimal.ZERO
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deposit should update account and save it")
    void depositShouldUpdateAndSaveAccount() {
        BankAccount account = new BankAccount(
                "ACC3002",
                "Deposit User",
                new BigDecimal("100.00")
        );

        when(repository.findByAccountNumber("ACC3002"))
                .thenReturn(Optional.of(account));

        bankService.deposit("ACC3002", new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), account.getBalance());
        verify(repository).save(account);
    }

    @Test
    @DisplayName("Unknown account should throw AccountNotFoundException")
    void unknownAccountShouldThrowException() {
        when(repository.findByAccountNumber("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> bankService.getBalance("UNKNOWN")
        );
    }

    @Test
    @DisplayName("Withdraw should update account and save it")
    void withdrawShouldUpdateAndSaveAccount() {
        BankAccount account = new BankAccount(
                "ACC3002",
                "Withdraw User",
                new BigDecimal("200.00")
        );

        when(repository.findByAccountNumber("ACC3002"))
                .thenReturn(Optional.of(account));

        bankService.withdraw("ACC3002", new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), account.getBalance());
        verify(repository).save(account);
    }

    @Test
    @DisplayName("Get balance should return correct balance")
    void getBalanceShouldReturnCorrectBalance() {
        BankAccount account = new BankAccount(
                "ACC3002",
                "Balance User",
                new BigDecimal("250.00")
        );

        when(repository.findByAccountNumber("ACC3002"))
                .thenReturn(Optional.of(account));

        BigDecimal balance = bankService.getBalance("ACC3002");

        assertEquals(new BigDecimal("250.00"), balance);
    }

    @Test
    @DisplayName("Get transaction history should return transaction history")
    void getTransactionHistoryShouldReturnHistory() {
        BankAccount account = new BankAccount(
                "ACC3002",
                "History User",
                new BigDecimal("100.00")
        );
        account.deposit(new BigDecimal("50.00"));

        when(repository.findByAccountNumber("ACC3002"))
                .thenReturn(Optional.of(account));

        java.util.List<com.vikaasni.bankapp.model.Transaction> history = bankService.getTransactionHistory("ACC3002");

        assertEquals(2, history.size());
        assertEquals(new BigDecimal("100.00"), history.get(0).getAmount());
        assertEquals(new BigDecimal("50.00"), history.get(1).getAmount());
    }

    @Test
    @DisplayName("Get all accounts should return all accounts from repository")
    void getAllAccountsShouldReturnList() {
        BankAccount account1 = new BankAccount("ACC3001", "User 1");
        BankAccount account2 = new BankAccount("ACC3002", "User 2");
        java.util.List<BankAccount> accountsList = java.util.List.of(account1, account2);

        when(repository.findAll()).thenReturn(accountsList);

        java.util.List<BankAccount> result = bankService.getAllAccounts();

        assertEquals(2, result.size());
        assertTrue(result.contains(account1));
        assertTrue(result.contains(account2));
    }

    @Test
    @DisplayName("Get account should return account when it exists")
    void getAccountShouldReturnAccount() {
        BankAccount account = new BankAccount(
                "ACC3002",
                "User",
                new BigDecimal("100.00")
        );

        when(repository.findByAccountNumber("ACC3002"))
                .thenReturn(Optional.of(account));

        BankAccount result = bankService.getAccount("ACC3002");

        assertEquals(account, result);
    }
}
