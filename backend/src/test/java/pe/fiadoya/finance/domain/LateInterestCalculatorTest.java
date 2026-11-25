package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class LateInterestCalculatorTest {
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
  void academic5759At35PercentForElevenDaysCosts053() {
    assertEquals(
        new BigDecimal("0.53"),
        new LateInterestCalculator()
            .calculate(
                new BigDecimal("57.59"),
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 12, 6),
                new Rate(new BigDecimal("35"), RateType.EFFECTIVE, 360, 0)));
  }

  @Test
  void futureObligationHasNoPenalty() {
    assertEquals(
        new BigDecimal("0.00"),
        new LateInterestCalculator()
            .calculate(
                new BigDecimal("57.59"),
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2026, 12, 6),
                rate()));
  }

  @Test
  void nominalPenaltyConvertsUsingItsCapitalization() {
    assertEquals(
        new BigDecimal("2.00"),
        new LateInterestCalculator()
            .calculate(
                new BigDecimal("100.00"),
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 12, 25),
                rate()));
  }
}
