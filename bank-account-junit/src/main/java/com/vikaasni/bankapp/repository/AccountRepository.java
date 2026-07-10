package com.vikaasni.bankapp.repository;

import com.vikaasni.bankapp.model.BankAccount;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void save(BankAccount account);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findAll();

    boolean existsByAccountNumber(String accountNumber);
}
