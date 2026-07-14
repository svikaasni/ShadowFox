package com.vikaasni.library.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FineServiceTest {
    @Test void noFineWhenReturnedOnDueDate() {
        assertEquals(0.0, FineService.calculateFine(LocalDate.of(2026,7,10), LocalDate.of(2026,7,10), 5.0));
    }
    @Test void calculatesFineForOverdueDays() {
        assertEquals(25.0, FineService.calculateFine(LocalDate.of(2026,7,10), LocalDate.of(2026,7,15), 5.0));
    }
    @Test void rejectsNegativeDailyFine() {
        assertThrows(IllegalArgumentException.class, () -> FineService.calculateFine(LocalDate.now(), LocalDate.now(), -1));
    }
}
