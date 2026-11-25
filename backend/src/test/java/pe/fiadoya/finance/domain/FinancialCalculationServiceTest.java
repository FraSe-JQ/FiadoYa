package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class FinancialCalculationServiceTest {
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
  void orchestrationKeepsPeriodZeroAtGraceEnd() {
    var s =
        new FinancialCalculationService()
            .calculate(
                new BigDecimal("164.00"),
                LocalDateTime.of(2026, 10, 6, 10, 38),
                terms(),
                Mode.INSTALLMENTS,
                3);
    assertEquals(LocalDate.of(2026, 10, 25), s.periodZero());
    assertEquals(new BigDecimal("172.76"), s.total());
  }

  @Test
  void monthEndCreatesOneObligation() {
    var s =
        new FinancialCalculationService()
            .calculate(
                new BigDecimal("164.00"),
                LocalDateTime.of(2026, 10, 6, 10, 38),
                terms(),
                Mode.MONTH_END,
                1);
    assertEquals(1, s.rows().size());
    assertEquals(new BigDecimal("166.07"), s.total());
  }

  @Test
  void excessiveTermIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new FinancialCalculationService()
                .calculate(
                    new BigDecimal("164.00"),
                    LocalDateTime.of(2026, 10, 6, 10, 38),
                    terms(),
                    Mode.INSTALLMENTS,
                    13));
  }
}
