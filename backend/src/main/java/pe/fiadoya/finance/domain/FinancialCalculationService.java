package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public final class FinancialCalculationService {
  /**
   * Coordina tasas, ciclo, gracia y cronograma, sin HTTP ni persistencia. Entrada: P monetario,
   * compra America/Lima, condiciones, modalidad y plazo autorizado. Salida: simulación con
   * TEA/TEM/TED decimales, periodo cero, capital e importes HALF_UP access 2 decimales. Periodos
   * comerciales 30/360 y vencimientos calendario. Tasa cero y ajuste final se delegan al calculador
   * francés. Fórmulas identificables en cada calculador. Informe 4.1–4.8.
   */
  public Schedule calculate(
      BigDecimal principal, LocalDateTime purchase, Terms terms, Mode mode, int months) {
    if (principal == null || principal.signum() <= 0 || principal.scale() > 2)
      throw new IllegalArgumentException("Capital positivo con hasta 2 decimales");
    if (months < 1 || months > terms.maxMonths())
      throw new IllegalArgumentException("Plazo superior al autorizado");
    Cycle cycle = new BillingCycleCalculator().calculate(purchase, terms);
    var grace =
        new GracePeriodCalculator()
            .calculate(
                principal, purchase.toLocalDate(), cycle.paymentDate(), terms.compensatory());
    var converter = new InterestRateConverter();
    BigDecimal monthlyRate = converter.effectiveRate(terms.compensatory(), 30);
    List<Row> rows;
    BigDecimal capital;
    if (mode == Mode.INSTALLMENTS) {
      capital = grace.capitalized();
      rows =
          new FrenchAmortizationCalculator()
              .schedule(capital, monthlyRate, months, cycle.paymentDate(), terms.paymentDay());
    } else {
      capital = principal;
      BigDecimal interest =
          new CompensatoryInterestCalculator()
              .calculate(principal, terms.compensatory(), grace.days());
      rows =
          List.of(
              new Row(
                  1,
                  cycle.paymentDate(),
                  principal,
                  interest,
                  principal,
                  principal.add(interest),
                  money(BigDecimal.ZERO)));
    }
    return new Schedule(
        principal,
        capital,
        mode == Mode.INSTALLMENTS ? grace.interest() : money(BigDecimal.ZERO),
        grace.days(),
        cycle.paymentDate(),
        cycle,
        converter.effectiveRate(terms.compensatory(), 360),
        monthlyRate,
        converter.effectiveRate(terms.compensatory(), 1),
        rows,
        rows.stream().map(Row::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
  }
}
