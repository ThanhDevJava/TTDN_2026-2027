package com.blueocean.library.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FineCalculatorTest {
  final FineCalculator f = new FineCalculator();
  final LocalDate due = LocalDate.of(2026, 10, 15);
  final BigDecimal rate = new BigDecimal("5000");

  @Test
  void early() {
    assertEquals(0, f.lateFee(due, due.minusDays(1), rate).signum());
  }

  @Test
  void onTime() {
    assertEquals(0, f.lateFee(due, due, rate).signum());
  }

  @Test
  void oneDayLate() {
    assertEquals(new BigDecimal("5000"), f.lateFee(due, due.plusDays(1), rate));
  }

  @Test
  void multipleDays() {
    assertEquals(new BigDecimal("15000"), f.lateFee(due, due.plusDays(3), rate));
  }

  @Test
  void crossMonth() {
    assertEquals(
        new BigDecimal("10000"),
        f.lateFee(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 2), rate));
  }

  @Test
  void rejectsNegativeRate() {
    assertThrows(IllegalArgumentException.class, () -> f.lateFee(due, due, rate.negate()));
  }
}
