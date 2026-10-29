package com.blueocean.library.service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class FineCalculator {
  public BigDecimal lateFee(LocalDate due, LocalDate returned, BigDecimal rate) {
    if (rate.signum() < 0) throw new IllegalArgumentException("Mức phạt không được âm");
    return rate.multiply(BigDecimal.valueOf(Math.max(0, ChronoUnit.DAYS.between(due, returned))));
  }
}
