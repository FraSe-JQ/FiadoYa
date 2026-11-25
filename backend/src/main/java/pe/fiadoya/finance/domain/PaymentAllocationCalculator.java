package pe.fiadoya.finance.domain;

import java.math.BigDecimal;

public final class PaymentAllocationCalculator {
  public record Allocation(
      BigDecimal penalty, BigDecimal interest, BigDecimal principal, BigDecimal remaining) {}

  /**
   * Imputa en orden mora, compensatorio y capital; flujo ordinario exige R=M+I+P. Entrada: importe
   * recibido R y conceptos monetarios no negativos, todos en la misma moneda. Salida: conceptos
   * cancelados y remanente cero. No convierte tasas ni dataías; no redondea otra vez los importes
   * previamente redondeados HALF_UP. Rechaza pagos parciales/excesos. Informe 4.8.
   */
  public Allocation allocate(
      BigDecimal received, BigDecimal penalty, BigDecimal interest, BigDecimal principal) {
    if (penalty.signum() < 0
        || interest.signum() < 0
        || principal.signum() < 0
        || received.compareTo(penalty.add(interest).add(principal)) != 0)
      throw new IllegalArgumentException("Pago exacto requerido; no se permiten pagos parciales");
    BigDecimal remaining = received.subtract(penalty);
    remaining = remaining.subtract(interest);
    remaining = remaining.subtract(principal);
    return new Allocation(penalty, interest, principal, remaining);
  }
}
