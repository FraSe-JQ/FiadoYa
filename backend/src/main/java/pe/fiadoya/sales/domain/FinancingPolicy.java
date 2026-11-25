package pe.fiadoya.sales.domain;

import java.math.BigDecimal;

/** Cash principal avoids charging interest twice through access credit list price. */
public final class FinancingPolicy {
  private FinancingPolicy() {}

  public static BigDecimal unitPrincipal(BigDecimal cashPrice, BigDecimal creditListPrice) {
    return cashPrice;
  }
}
