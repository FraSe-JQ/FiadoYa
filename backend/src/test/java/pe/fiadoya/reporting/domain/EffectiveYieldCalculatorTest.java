package pe.fiadoya.reporting.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EffectiveYieldCalculatorTest {
  @Test
  void yieldSolverExpandsItsBracketInsteadOfSilentlyCappingTheRate() {
    var result =
        new EffectiveYieldCalculator()
            .annualYield(
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 1, 15),
                List.of(
                    new EffectiveYieldCalculator.Flow(
                        LocalDate.of(2027, 1, 15), new BigDecimal("2000000.00"))));
    assertEquals(new BigDecimal("1999.0000000"), result.setScale(7, RoundingMode.HALF_UP));
  }

  @Test
  void thirtyDayTwoPercentCashFlowHasEquivalentAnnualYield() {
    var calculator = new EffectiveYieldCalculator();
    var result =
        calculator.annualYield(
            new BigDecimal("1000.00"),
            LocalDate.of(2026, 1, 15),
            List.of(
                new EffectiveYieldCalculator.Flow(
                    LocalDate.of(2026, 2, 15), new BigDecimal("1020.00"))));
    assertEquals(new BigDecimal("0.268241795"), result.setScale(9, RoundingMode.HALF_UP));
  }

  @Test
  void flowsWithNoFinancingReturnZeroYield() {
    var result =
        new EffectiveYieldCalculator()
            .annualYield(
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 1, 15),
                List.of(
                    new EffectiveYieldCalculator.Flow(
                        LocalDate.of(2026, 2, 15), new BigDecimal("1000.00"))));
    assertEquals(0, result.setScale(12, RoundingMode.HALF_UP).signum());
  }
}
