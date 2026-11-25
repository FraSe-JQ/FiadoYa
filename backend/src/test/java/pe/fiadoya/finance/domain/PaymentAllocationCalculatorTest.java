package pe.fiadoya.finance.domain;

import static org.junit.jupiter.api.Assertions.*;
import static pe.fiadoya.finance.domain.FinanceEngine.*;

import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;

class PaymentAllocationCalculatorTest {
  static Rate rate() {
    return new Rate(new BigDecimal("24"), RateType.NOMINAL, 360, 30);
  }

  static Terms terms() {
    return new Terms(
        "PEN",
        new BigDecimal("1000"),
        12,
        15,
        LocalTime.of(23, 59),
        25,
        rate(),
        new Rate(new BigDecimal("35"), RateType.EFFECTIVE, 360, 0));
  }

  @Test
  void academic5812PaysPenaltyThenInterestThenPrincipal() {
    var x =
        new PaymentAllocationCalculator()
            .allocate(
                new BigDecimal("58.12"),
                new BigDecimal("0.53"),
                new BigDecimal("3.32"),
                new BigDecimal("54.27"));
    assertEquals(new BigDecimal("0.00"), x.remaining());
    assertEquals(new BigDecimal("54.27"), x.principal());
  }

  @Test
  void insufficientPaymentIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new PaymentAllocationCalculator()
                .allocate(
                    new BigDecimal("58.00"),
                    new BigDecimal("0.53"),
                    new BigDecimal("3.32"),
                    new BigDecimal("54.27")));
  }
}
