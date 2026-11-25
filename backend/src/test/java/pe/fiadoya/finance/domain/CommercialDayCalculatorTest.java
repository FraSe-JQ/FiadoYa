package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class CommercialDayCalculatorTest {
  static Rate rate() {
    return new Rate(new BigDecimal("24"), RateType.NOMINAL, 360, 30);
  }

  static Terms terms() {
    return new Terms(
        "PEN",
        new BigDecimal("1000"),
        12,
        15,
        LocalTime.of(23, 59),
        25,
        rate(),
        new Rate(new BigDecimal("35"), RateType.EFFECTIVE, 360, 0));
  }

  @Test
  void academicLatePaymentHasElevenCommercialDays() {
    assertEquals(
        11,
        new CommercialDayCalculator()
            .between(LocalDate.of(2026, 11, 25), LocalDate.of(2026, 12, 6)));
  }

  @Test
  void februaryMonthEndToMarchMonthEndIsThirty() {
    assertEquals(
        30,
        new CommercialDayCalculator()
            .between(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31)));
  }

  @Test
  void leapYearAndYearChangeAreNormalized() {
    assertEquals(
        30,
        new CommercialDayCalculator()
            .between(LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29)));
    assertEquals(
        1,
        new CommercialDayCalculator()
            .between(LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 1)));
  }
}
