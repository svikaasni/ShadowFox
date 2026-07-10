package com.vikaasni.bankapp.model;

import com.vikaasni.bankapp.exception.InsufficientFundsException;
import com.vikaasni.bankapp.exception.InvalidAmountException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {
    private BankAccount account;

    @BeforeEach
    void setUp() {
        account = new BankAccount(
                "ACC1001",
                "Vikaasni",
                new BigDecimal("1000.00")
        );
    }

    @AfterEach
    void tearDown() {
        account = null;
    }

    @Test
    @DisplayName("Deposit should increase the account balance")
    void depositShouldIncreaseBalance() {
        account.deposit(new BigDecimal("500.00"));

        assertEquals(
                new BigDecimal("1500.00"),
                account.getBalance()
        );
    }

    @Test
    @DisplayName("Withdrawal should decrease the account balance")
    void withdrawalShouldDecreaseBalance() {
        account.withdraw(new BigDecimal("250.00"));

        assertEquals(
                new BigDecimal("750.00"),
                account.getBalance()
        );
    }

    @Test
    @DisplayName("Balance inquiry should return the current balance")
    void balanceInquiryShouldReturnCurrentBalance() {
        assertEquals(
                new BigDecimal("1000.00"),
                account.getBalance()
        );
    }

    @Test
    @DisplayName("Withdrawing more than balance should throw exception")
    void withdrawingMoreThanBalanceShouldThrowException() {
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(new BigDecimal("1500.00"))
        );

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        assertEquals(
                new BigDecimal("1000.00"),
                account.getBalance()
        );
    }

    @Test
    @DisplayName("Zero deposit should throw InvalidAmountException")
    void zeroDepositShouldThrowException() {
        assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Negative withdrawal should throw InvalidAmountException")
    void negativeWithdrawalShouldThrowException() {
        assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(new BigDecimal("-50.00"))
        );
    }

    @Test
    @DisplayName("Deposit should add one transaction to history")
    void depositShouldAddTransactionToHistory() {
        int initialSize = account.getTransactionHistory().size();

        account.deposit(new BigDecimal("100.00"));

        assertEquals(
                initialSize + 1,
                account.getTransactionHistory().size()
        );

        Transaction latest = account.getTransactionHistory()
                .get(account.getTransactionHistory().size() - 1);

        assertEquals(TransactionType.DEPOSIT, latest.getType());
        assertEquals(new BigDecimal("100.00"), latest.getAmount());
        assertEquals(
                new BigDecimal("1100.00"),
                latest.getBalanceAfterTransaction()
        );
    }

    @Test
    @DisplayName("Withdrawal should add a withdrawal transaction")
    void withdrawalShouldAddTransactionToHistory() {
        account.withdraw(new BigDecimal("200.00"));

        Transaction latest = account.getTransactionHistory()
                .get(account.getTransactionHistory().size() - 1);

        assertEquals(TransactionType.WITHDRAWAL, latest.getType());
        assertEquals(new BigDecimal("200.00"), latest.getAmount());
        assertEquals(
                new BigDecimal("800.00"),
                latest.getBalanceAfterTransaction()
        );
    }

    @Test
    @DisplayName("Transaction history should be read-only")
    void transactionHistoryShouldBeUnmodifiable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> account.getTransactionHistory().clear()
        );
    }

    @Test
    @DisplayName("Constructor with null/blank account number should throw IllegalArgumentException")
    void constructorWithNullOrBlankAccountNumberShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BankAccount(null, "Holder", new BigDecimal("100.00"))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new BankAccount("   ", "Holder", new BigDecimal("100.00"))
        );
    }

    @Test
    @DisplayName("Constructor with null/blank account holder name should throw IllegalArgumentException")
    void constructorWithNullOrBlankAccountHolderNameShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BankAccount("ACC1002", null, new BigDecimal("100.00"))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new BankAccount("ACC1002", "   ", new BigDecimal("100.00"))
        );
    }

    @Test
    @DisplayName("Constructor with negative opening balance should throw InvalidAmountException")
    void constructorWithNegativeOpeningBalanceShouldThrowException() {
        assertThrows(
                InvalidAmountException.class,
                () -> new BankAccount("ACC1002", "Holder", new BigDecimal("-10.00"))
        );
    }

    @Test
    @DisplayName("Constructor without opening balance should initialize to zero")
    void constructorWithoutOpeningBalanceShouldInitializeToZero() {
        BankAccount zeroAccount = new BankAccount("ACC1002", "Holder");
        assertEquals(new BigDecimal("0.00"), zeroAccount.getBalance());
        assertTrue(zeroAccount.getTransactionHistory().isEmpty());
    }
}
