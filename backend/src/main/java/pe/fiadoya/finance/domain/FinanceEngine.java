package pe.fiadoya.finance.domain;

import java.math.*;
import java.time.*;
import java.util.*;

/** Tipos financieros y fachada; las tasas de entrada son porcentajes, no fracciones decimales. */
public final class FinanceEngine {
  public static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);

  public enum RateType {
    NOMINAL,
    EFFECTIVE
  }

  public enum Mode {
    MONTH_END,
    INSTALLMENTS
  }

  public record Rate(BigDecimal percent, RateType type, int periodDays, int capitalizationDays) {
    public Rate {
      if (percent == null
          || type == null
          || percent.signum() < 0
          || percent.compareTo(new BigDecimal("1000")) > 0
          || periodDays < 1
          || periodDays > 360)
        throw new IllegalArgumentException("Tasa inválida: porcentaje 0–1000 y periodo 1–360 días");
      if (type == RateType.NOMINAL
          && (capitalizationDays < 1
              || capitalizationDays > periodDays
              || periodDays % capitalizationDays != 0))
        throw new IllegalArgumentException(
            "La capitalización debe dividir exactamente el periodo nominal");
    }

    /**
     * Obtiene i(d) decimal mediante InterestRateConverter: (1+i)^(d/D)-1 para efectiva, o
     * (1+j/(D/c))^(d/c)-1 para nominal. Entrada porcentual, días comerciales no negativos; salida
     * con precisión 34, sin redondeo monetario. Cero días/tasa produce cero. Informe 4.1.
     */
    public BigDecimal effective(int days) {
      return new InterestRateConverter().effectiveRate(this, days);
    }
  }

  public record Terms(
      String currency,
      BigDecimal creditLimit,
      int maxMonths,
      int cutoffDay,
      LocalTime cutoffTime,
      int paymentDay,
      Rate compensatory,
      Rate penalty) {
    public Terms {
      if (!Set.of("PEN", "USD").contains(currency)
          || creditLimit == null
          || creditLimit.signum() <= 0
          || maxMonths < 1
          || maxMonths > 120
          || cutoffDay < 1
          || cutoffDay > 31
          || paymentDay < 1
          || paymentDay > 31
          || cutoffTime == null
          || compensatory == null
          || penalty == null)
        throw new IllegalArgumentException("Condiciones de crédito inválidas");
    }
  }

  public record Cycle(LocalDateTime cutoff, LocalDate paymentDate) {}

  public record Row(
      int number,
      LocalDate dueDate,
      BigDecimal opening,
      BigDecimal interest,
      BigDecimal principal,
      BigDecimal amount,
      BigDecimal closing) {}

  public record Schedule(
      BigDecimal financed,
      BigDecimal capitalized,
      BigDecimal graceInterest,
      int graceDays,
      LocalDate periodZero,
      Cycle cycle,
      BigDecimal tea,
      BigDecimal tem,
      BigDecimal ted,
      List<Row> rows,
      BigDecimal total) {}

  /**
   * Redondea un importe a dos decimales HALF_UP: resultado=round(importe,2). Entrada y salida en la
   * misma moneda; no convierte tasas ni días. Convención de todo el informe.
   */
  public static BigDecimal money(BigDecimal amount) {
    return amount.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Obtiene fecha calendario con día=min(díaPactado,últimoDíaDelMes), incluido febrero. Entrada:
   * mes y día pactado 1–31. Salida: fecha sin tasa ni redondeo monetario. Informe 4.2.
   */
  public static LocalDate day(YearMonth month, int day) {
    return month.atDay(Math.min(day, month.lengthOfMonth()));
  }

  /**
   * Delega d=360*Δaño+30*Δmes+Δdía normalizado al calculador comercial. Entrada: dos fechas;
   * salida: entero con signo, sin tasas ni redondeo. Finales de mes=30. Informe 4.2.
   */
  public static int days(LocalDate startDate, LocalDate endDate) {
    return new CommercialDayCalculator().between(startDate, endDate);
  }

  /**
   * Delega compra<=corte y pago>=fecha de corte a BillingCycleCalculator. Entrada local Lima y
   * condiciones; salida calendario; sin tasas ni redondeo. Informe 4.2.
   */
  public static Cycle cycle(LocalDateTime purchase, Terms terms) {
    return new BillingCycleCalculator().calculate(purchase, terms);
  }

  /**
   * Delega el proceso P, Pg=P*(1+i(d)) y cuota francesa al coordinador financiero. Entrada
   * monetaria, fecha Lima y tasas porcentuales con periodos; salida cronograma 30/360, importes
   * HALF_UP a dos decimales, precisión intermedia 34. Incluye tasa cero. Informe 4.1–4.8.
   */
  public static Schedule simulate(
      BigDecimal principal, LocalDateTime purchase, Terms terms, Mode mode, int months) {
    return new FinancialCalculationService().calculate(principal, purchase, terms, mode, months);
  }

  /**
   * Delega mora=M*((1+TEAm)^(max(0,d)/360)-1). Entrada: obligación monetaria vencida, fechas y tasa
   * porcentual con periodicidad; salida HALF_UP a dos decimales. Usa 30/360; sin atraso devuelve
   * cero, sin afectar cuotas futuras. Informe 4.6.
   */
  public static BigDecimal penalty(BigDecimal debt, LocalDate due, LocalDate paid, Rate rate) {
    return new LateInterestCalculator().calculate(debt, due, paid, rate);
  }
}
