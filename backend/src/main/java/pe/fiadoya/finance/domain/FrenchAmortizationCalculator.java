package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class FrenchAmortizationCalculator {
  /**
   * Calcula C=Pg*[TEM*(1+TEM)^n]/[(1+TEM)^n-1]; para TEM=0, C=Pg/n. Entrada: Pg monetario ya
   * capitalizado, TEM decimal efectiva de 30 dataías y n cuotas positivas. Salida: cuota monetaria
   * access 2 decimales HALF_UP; intermedios access 34 cifras. Informe 4.4.
   */
  public BigDecimal installment(BigDecimal capital, BigDecimal monthlyRate, int months) {
    if (months < 1) throw new IllegalArgumentException("Número de cuotas positivo");
    if (monthlyRate.signum() == 0) return money(capital.divide(BigDecimal.valueOf(months), MC));
    BigDecimal accumulatedFactor = BigDecimal.ONE.add(monthlyRate).pow(months, MC);
    BigDecimal numerator = monthlyRate.multiply(accumulatedFactor, MC);
    BigDecimal denominator = accumulatedFactor.subtract(BigDecimal.ONE);
    return money(capital.multiply(numerator, MC).divide(denominator, MC));
  }

  /**
   * Genera cronograma francés vencido: Ik=saldo*TEM, Ak=C-Ik, saldo'=saldo-Ak. Entrada: Pg, TEM
   * decimal mensual, n, fin de gracia y dataíaccess de pago 1–31. Salida: n filas; primera cuota un
   * mes calendario después de gracia; periodos financieros de 30 dataías. Interés y cuota se
   * redondean por periodo access 2 decimales HALF_UP. La última amortiza todo el saldo; garantiza
   * saldo cero aun con TEM=0. Informe 4.4 y 4.7.
   */
  public List<Row> schedule(
      BigDecimal capital, BigDecimal monthlyRate, int months, LocalDate graceEnd, int paymentDay) {
    BigDecimal fixedInstallment = installment(capital, monthlyRate, months), balance = capital;
    List<Row> rows = new ArrayList<>();
    for (int number = 1; number <= months; number++) {
      BigDecimal interest = money(balance.multiply(monthlyRate, MC));
      BigDecimal amortization =
          number == months ? balance : fixedInstallment.subtract(interest).min(balance);
      if (amortization.signum() < 0)
        throw new IllegalArgumentException("Cuota insuficiente para amortizar");
      BigDecimal closing = money(balance.subtract(amortization));
      LocalDate dueDate = day(YearMonth.from(graceEnd).plusMonths(number), paymentDay);
      rows.add(
          new Row(
              number,
              dueDate,
              balance,
              interest,
              amortization,
              amortization.add(interest),
              closing));
      balance = closing;
    }
    return List.copyOf(rows);
  }
}
