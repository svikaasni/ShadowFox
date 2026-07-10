package com.vikaasni.bankapp.model;

import com.vikaasni.bankapp.exception.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankAccountConcurrencyTest {

    @Test
    @DisplayName("Two simultaneous withdrawals must not overdraw the account")
    void simultaneousWithdrawalsShouldBeThreadSafe() throws InterruptedException {
        BankAccount account = new BankAccount(
                "ACC2001",
                "Concurrency User",
                new BigDecimal("1000.00")
        );

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger successfulWithdrawals = new AtomicInteger();
        AtomicInteger failedWithdrawals = new AtomicInteger();

        Runnable withdrawalTask = () -> {
            try {
                startSignal.await();
                account.withdraw(new BigDecimal("700.00"));
                successfulWithdrawals.incrementAndGet();
            } catch (InsufficientFundsException exception) {
                failedWithdrawals.incrementAndGet();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        };

        Thread firstThread = new Thread(withdrawalTask);
        Thread secondThread = new Thread(withdrawalTask);

        firstThread.start();
        secondThread.start();

        startSignal.countDown();

        firstThread.join();
        secondThread.join();

        assertEquals(1, successfulWithdrawals.get());
        assertEquals(1, failedWithdrawals.get());
        assertEquals(new BigDecimal("300.00"), account.getBalance());
    }
}
