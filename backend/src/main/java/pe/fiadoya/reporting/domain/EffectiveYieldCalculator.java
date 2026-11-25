package pe.fiadoya.reporting.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Rendimiento contractual, sin costos operativos ni inferencia de utilidad del comercio. */
public final class EffectiveYieldCalculator {
  public record Flow(LocalDate date, BigDecimal amount) {}

  /** Resuelve -P+sum(Fk/(1+r)^(dk/360))=0 por bisección decimal; r es TEA decimal. */
  public BigDecimal annualYield(BigDecimal principal, LocalDate origin, List<Flow> flows) {
    BigDecimal low = BigDecimal.ZERO, high = new BigDecimal("1000");
    if (principal.signum() <= 0
        || flows.isEmpty()
        || flows.stream()
            .anyMatch(flow -> days(origin, flow.date()) <= 0 || flow.amount().signum() < 0))
      throw new IllegalArgumentException(
          "Capital positivo y flujos no negativos posteriores al desembolso en días comerciales");
    if (presentValue(principal, origin, flows, low).signum() < 0)
      throw new IllegalArgumentException("Los flujos no permiten un rendimiento no negativo");
    int expansions = 0;
    while (presentValue(principal, origin, flows, high).signum() > 0 && expansions++ < 64)
      high = high.multiply(BigDecimal.valueOf(2));
    if (presentValue(principal, origin, flows, high).signum() > 0)
      throw new IllegalArgumentException("No se pudo acotar el rendimiento de los flujos");
    for (int iteration = 0; iteration < 180; iteration++) {
      BigDecimal middle = low.add(high).divide(BigDecimal.valueOf(2), MC);
      BigDecimal value = presentValue(principal, origin, flows, middle);
      if (value.signum() > 0) low = middle;
      else high = middle;
    }
    return low.add(high).divide(BigDecimal.valueOf(2), MC);
  }

  private BigDecimal presentValue(
      BigDecimal principal, LocalDate origin, List<Flow> flows, BigDecimal annualRate) {
    BigDecimal value = principal.negate();
    for (Flow flow : flows) {
      BigDecimal exponent =
          BigDecimal.valueOf(days(origin, flow.date())).divide(BigDecimal.valueOf(360), MC);
      value =
          value.add(
              flow.amount()
                  .divide(BigDecimalMath.pow(BigDecimal.ONE.add(annualRate), exponent, MC), MC));
    }
    return value;
  }
}
