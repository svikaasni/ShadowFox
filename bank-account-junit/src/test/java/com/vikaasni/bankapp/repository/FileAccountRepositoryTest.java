package com.vikaasni.bankapp.repository;

import com.vikaasni.bankapp.model.BankAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileAccountRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Save and load accounts successfully from file")
    void saveAndLoadAccount() {
        Path filePath = tempDir.resolve("accounts.txt");
        FileAccountRepository repository = new FileAccountRepository(filePath);

        BankAccount account = new BankAccount("ACC5001", "User Temp", new BigDecimal("350.00"));
        repository.save(account);

        assertTrue(repository.existsByAccountNumber("ACC5001"));
        Optional<BankAccount> found = repository.findByAccountNumber("ACC5001");
        assertTrue(found.isPresent());
        assertEquals("ACC5001", found.get().getAccountNumber());
        assertEquals("User Temp", found.get().getAccountHolderName());
        assertEquals(new BigDecimal("350.00"), found.get().getBalance());

        // Create new repository loading from the same file
        FileAccountRepository reloadRepository = new FileAccountRepository(filePath);
        assertTrue(reloadRepository.existsByAccountNumber("ACC5001"));
        Optional<BankAccount> foundReloaded = reloadRepository.findByAccountNumber("ACC5001");
        assertTrue(foundReloaded.isPresent());
        assertEquals(new BigDecimal("350.00"), foundReloaded.get().getBalance());
    }

    @Test
    @DisplayName("Non-existent file should load empty accounts list")
    void nonExistentFileShouldBeEmpty() {
        Path nonExistent = tempDir.resolve("does_not_exist.txt");
        FileAccountRepository repository = new FileAccountRepository(nonExistent);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Empty lines or lines with length != 3 should be skipped")
    void skippedLinesTest() throws IOException {
        Path filePath = tempDir.resolve("skipped.txt");
        List<String> lines = List.of(
                "ACC5002|User Skip|100.00",
                "",
                "ACC5003|User Bad", // only 2 fields
                "ACC5004|User OK|200.00"
        );
        Files.write(filePath, lines, StandardCharsets.UTF_8);

        FileAccountRepository repository = new FileAccountRepository(filePath);
        assertEquals(2, repository.findAll().size());
        assertTrue(repository.existsByAccountNumber("ACC5002"));
        assertTrue(repository.existsByAccountNumber("ACC5004"));
        assertFalse(repository.existsByAccountNumber("ACC5003"));
    }

    @Test
    @DisplayName("Malformed balance in file should throw IllegalStateException")
    void malformedBalanceShouldThrowException() throws IOException {
        Path filePath = tempDir.resolve("malformed.txt");
        List<String> lines = List.of(
                "ACC5002|User Malformed|100.00abc"
        );
        Files.write(filePath, lines, StandardCharsets.UTF_8);

        assertThrows(
                IllegalStateException.class,
                () -> new FileAccountRepository(filePath)
        );
    }
}
