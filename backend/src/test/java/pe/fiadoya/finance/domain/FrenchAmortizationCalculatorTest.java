package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class FrenchAmortizationCalculatorTest {
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
  void academicScheduleHasCorrectAmountsDatesAndZeroFinalBalance() {
    var rows =
        new FrenchAmortizationCalculator()
            .schedule(
                new BigDecimal("166.07"),
                new BigDecimal("0.02"),
                3,
                LocalDate.of(2026, 10, 25),
                25);
    assertEquals(new BigDecimal("57.59"), rows.get(0).amount());
    assertEquals(new BigDecimal("57.59"), rows.get(1).amount());
    assertEquals(new BigDecimal("57.58"), rows.get(2).amount());
    assertEquals(new BigDecimal("3.32"), rows.get(0).interest());
    assertEquals(new BigDecimal("54.27"), rows.get(0).principal());
    assertEquals(new BigDecimal("111.80"), rows.get(0).closing());
    assertEquals(new BigDecimal("0.00"), rows.get(2).closing());
    assertEquals(LocalDate.of(2027, 1, 25), rows.get(2).dueDate());
    assertEquals(
        new BigDecimal("172.76"),
        rows.stream().map(Row::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  @Test
  void zeroRateStillAdjustsLastInstallment() {
    var rows =
        new FrenchAmortizationCalculator()
            .schedule(new BigDecimal("100.00"), BigDecimal.ZERO, 3, LocalDate.of(2026, 1, 31), 31);
    assertEquals(new BigDecimal("33.34"), rows.get(2).amount());
    assertEquals(LocalDate.of(2026, 2, 28), rows.get(0).dueDate());
  }
}
