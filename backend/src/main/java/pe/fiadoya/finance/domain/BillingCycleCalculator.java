package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.time.*;

public final class BillingCycleCalculator {
  /**
   * Asigna ciclo comparando compra<=corte; después del corte usa el siguiente mes calendario.
   * Entrada: fecha-hora local America/Lima y condiciones; salida: corte y pago >= fecha del corte.
   * Días inexistentes se limitan al último dataíaccess del mes; no se suman 30 dataías calendario.
   * Sin tasas, importes ni redondeo. La igualdad exacta pertenece al ciclo actual. Informe 4.2.
   */
  public Cycle calculate(LocalDateTime purchase, Terms terms) {
    YearMonth month = YearMonth.from(purchase);
    LocalDateTime cutoff = day(month, terms.cutoffDay()).atTime(terms.cutoffTime());
    if (purchase.isAfter(cutoff)) {
      month = month.plusMonths(1);
      cutoff = day(month, terms.cutoffDay()).atTime(terms.cutoffTime());
    }
    LocalDate paymentDate = day(month, terms.paymentDay());
    if (paymentDate.isBefore(cutoff.toLocalDate()))
      paymentDate = day(month.plusMonths(1), terms.paymentDay());
    return new Cycle(cutoff, paymentDate);
  }
}
