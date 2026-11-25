package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class InterestRateConverterTest {
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
  void annualNominal24WithMonthlyCapitalizationProducesMonthly2Percent() {
    assertEquals(
        0, new BigDecimal("0.02").compareTo(new InterestRateConverter().effectiveRate(rate(), 30)));
  }

  @Test
  void quarterlyNominal6WithMonthlyCapitalizationProducesMonthly2Percent() {
    assertEquals(
        0,
        new BigDecimal("0.02")
            .compareTo(
                new InterestRateConverter()
                    .effectiveRate(new Rate(new BigDecimal("6"), RateType.NOMINAL, 90, 30), 30)));
  }

  @Test
  void effectiveMonthly2ProducesAnnual26824179456Percent() {
    assertEquals(
        new BigDecimal("0.268241795"),
        new InterestRateConverter()
            .effectiveRate(new Rate(new BigDecimal("2"), RateType.EFFECTIVE, 30, 0), 360)
            .setScale(9, java.math.RoundingMode.HALF_UP));
  }

  @Test
  void incompatibleNominalCapitalizationIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new Rate(BigDecimal.ONE, RateType.NOMINAL, 90, 28));
  }
}
