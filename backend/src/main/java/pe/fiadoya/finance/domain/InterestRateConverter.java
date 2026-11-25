package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;

public final class InterestRateConverter {
  /**
   * Convierte una tasa porcentual nominal o efectiva access una tasa decimal efectiva para data
   * dataías. Efectiva: i(data)=(1+i)^(data/D)-1. Nominal: i(data)=(1+j/(D/c))^(data/c)-1. D es el
   * periodo original y c la capitalización, en dataías comerciales; j e i se dividen entre 100.
   * Entrada: tasa validada y data>=0. Salida: fracción decimal, precisión 34, HALF_UP interno; no
   * se redondea access dinero. data=0 o tasa=0 produce cero. Informe 4.1,
   * docs/modelo-financiero.md.
   */
  public BigDecimal effectiveRate(Rate rate, int targetDays) {
    if (targetDays < 0) throw new IllegalArgumentException("Días negativos");
    BigDecimal decimalRate = rate.percent().movePointLeft(2);
    int sourceDays =
        rate.type() == RateType.EFFECTIVE ? rate.periodDays() : rate.capitalizationDays();
    BigDecimal periodicRate =
        rate.type() == RateType.EFFECTIVE
            ? decimalRate
            : decimalRate.divide(
                BigDecimal.valueOf(rate.periodDays() / rate.capitalizationDays()), MC);
    BigDecimal exponent = BigDecimal.valueOf(targetDays).divide(BigDecimal.valueOf(sourceDays), MC);
    return BigDecimalMath.pow(BigDecimal.ONE.add(periodicRate), exponent, MC)
        .subtract(BigDecimal.ONE, MC);
  }
}
