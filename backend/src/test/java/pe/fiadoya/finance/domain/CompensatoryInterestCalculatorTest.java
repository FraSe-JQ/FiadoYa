package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class CompensatoryInterestCalculatorTest {
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
  void monthEnd164ForNineteenDaysCosts207() {
    assertEquals(
        new BigDecimal("2.07"),
        new CompensatoryInterestCalculator().calculate(new BigDecimal("164.00"), rate(), 19));
  }

  @Test
  void zeroRateOrZeroDaysCostsZero() {
    assertEquals(
        new BigDecimal("0.00"),
        new CompensatoryInterestCalculator().calculate(new BigDecimal("164.00"), rate(), 0));
  }
}
