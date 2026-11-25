package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;

public final class CompensatoryInterestCalculator {
  /**
   * Calcula I=P*[(1+TEA)^(data/360)-1] para pago único; la conversión admite tasa nominal o
   * efectiva. Entrada: P monetario, tasa porcentual con periodo y data comerciales >=0. Salida:
   * interés monetario access 2 decimales HALF_UP, precisión interna 34. Tasa o dataías cero
   * producen cero. Informe 4.5.
   */
  public BigDecimal calculate(BigDecimal principal, Rate rate, int days) {
    return money(principal.multiply(new InterestRateConverter().effectiveRate(rate, days), MC));
  }
}
