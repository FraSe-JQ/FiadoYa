package pe.fiadoya.finance.domain;

import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LateInterestCalculator {
  /**
   * Calcula mora=M*[(1+TEAm)^(max(0,data)/360)-1], data comercial entre vencimiento y pago real.
   * Entrada: importe vencido M (capital e interés de esa obligación), tasa porcentual pactada y
   * fechas. Salida: mora monetaria 2 decimales HALF_UP; intermedios 34. No aplica sobre cuotas
   * futuras; pago puntual/anticipado produce cero. Admite mora nominal o efectiva. Informe 4.6.
   */
  public BigDecimal calculate(
      BigDecimal overdueAmount, LocalDate dueDate, LocalDate paymentDate, Rate penaltyRate) {
    int lateDays = Math.max(0, new CommercialDayCalculator().between(dueDate, paymentDate));
    return money(
        overdueAmount.multiply(
            new InterestRateConverter().effectiveRate(penaltyRate, lateDays), MC));
  }
}
