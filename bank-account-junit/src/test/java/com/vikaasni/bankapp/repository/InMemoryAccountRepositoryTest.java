package com.vikaasni.bankapp.repository;

import com.vikaasni.bankapp.model.BankAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountRepositoryTest {
    private InMemoryAccountRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();
    }

    @Test
    @DisplayName("Saving an account should store it in memory")
    void saveAndFindAccount() {
        BankAccount account = new BankAccount("ACC4001", "User A", new BigDecimal("500.00"));
        repository.save(account);

        Optional<BankAccount> found = repository.findByAccountNumber("ACC4001");
        assertTrue(found.isPresent());
        assertEquals("ACC4001", found.get().getAccountNumber());
        assertEquals("User A", found.get().getAccountHolderName());
        assertEquals(new BigDecimal("500.00"), found.get().getBalance());
    }

    @Test
    @DisplayName("Finding an unknown account should return empty Optional")
    void findNonExistentAccount() {
        Optional<BankAccount> found = repository.findByAccountNumber("NON_EXISTENT");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("existsByAccountNumber should return true only for existing accounts")
    void existsByAccountNumber() {
        BankAccount account = new BankAccount("ACC4002", "User B", new BigDecimal("100.00"));
        repository.save(account);

        assertTrue(repository.existsByAccountNumber("ACC4002"));
        assertFalse(repository.existsByAccountNumber("ACC4003"));
    }

    @Test
    @DisplayName("findAll should return all saved accounts")
    void findAllAccounts() {
        BankAccount account1 = new BankAccount("ACC4004", "User C");
        BankAccount account2 = new BankAccount("ACC4005", "User D");

        repository.save(account1);
        repository.save(account2);

        List<BankAccount> all = repository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(a -> a.getAccountNumber().equals("ACC4004")));
        assertTrue(all.stream().anyMatch(a -> a.getAccountNumber().equals("ACC4005")));
    }
}
