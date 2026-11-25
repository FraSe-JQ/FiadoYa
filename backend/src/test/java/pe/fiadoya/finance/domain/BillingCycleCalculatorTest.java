package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class BillingCycleCalculatorTest {
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
  void purchaseExactlyAtCutoffStaysInCurrentCycle() {
    assertEquals(
        LocalDate.of(2026, 10, 25),
        new BillingCycleCalculator()
            .calculate(LocalDateTime.of(2026, 10, 15, 23, 59), terms())
            .paymentDate());
  }

  @Test
  void purchaseAfterCutoffMovesToNextMonth() {
    assertEquals(
        LocalDate.of(2026, 11, 25),
        new BillingCycleCalculator()
            .calculate(LocalDateTime.of(2026, 10, 15, 23, 59, 1), terms())
            .paymentDate());
  }

  @Test
  void decemberAfterCutoffMovesToJanuary() {
    assertEquals(
        LocalDate.of(2027, 1, 25),
        new BillingCycleCalculator()
            .calculate(LocalDateTime.of(2026, 12, 16, 0, 0), terms())
            .paymentDate());
  }
}
