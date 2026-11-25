package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class GracePeriodCalculator {
  public record Grace(int days, BigDecimal interest, BigDecimal capitalized) {}

  /**
   * Capitaliza gracia total: Pg=P*(1+TEA)^(data/360), I=Pg-P. Entrada: capital monetario P, compra,
   * fin de gracia y tasa porcentual con periodicidad explícita. data usa CommercialDayCalculator.
   * Salida: dataías, interés y Pg monetarios con 2 decimales HALF_UP; precisión intermedia 34.
   * Gracia cero produce I=0. Informe 4.3.
   */
  public Grace calculate(BigDecimal principal, LocalDate purchase, LocalDate end, Rate rate) {
    int days = new CommercialDayCalculator().between(purchase, end);
    if (days < 0) throw new IllegalArgumentException("Fin de gracia anterior a compra");
    BigDecimal interest =
        money(principal.multiply(new InterestRateConverter().effectiveRate(rate, days), MC));
    return new Grace(days, interest, principal.add(interest));
  }
}
