package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class GracePeriodCalculatorTest {
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
  void academicPrincipal164CapitalizesNineteenDaysTo16607() {
    var g =
        new GracePeriodCalculator()
            .calculate(
                new BigDecimal("164.00"),
                LocalDate.of(2026, 10, 6),
                LocalDate.of(2026, 10, 25),
                rate());
    assertEquals(19, g.days());
    assertEquals(new BigDecimal("2.07"), g.interest());
    assertEquals(new BigDecimal("166.07"), g.capitalized());
  }
}
