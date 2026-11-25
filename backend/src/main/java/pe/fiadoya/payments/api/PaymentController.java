package pe.fiadoya.payments.api;

import jakarta.validation.Valid;
import java.math.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fiadoya.accounts.application.CreditService;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.payments.application.PaymentService;
import pe.fiadoya.sales.application.SalesService;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@RestController
@RequestMapping("/api/stores/{storeId}")
@Transactional
public class PaymentController {
  private final Data data;
  private final Access access;
  private final SalesService sales;
  private final PaymentService payments;
  private final CreditService credit;
  private final OperationalClock clock;

  public PaymentController(
      Data data,
      Access access,
      SalesService sales,
      PaymentService payments,
      CreditService credit,
      OperationalClock clock) {
    this.data = data;
    this.access = access;
    this.sales = sales;
    this.payments = payments;
    this.credit = credit;
    this.clock = clock;
  }

  @GetMapping("/accounts/{id}/quote")
  public Object quote(@PathVariable Long storeId, @PathVariable Long id) {
    return payments.quote(storeId, id);
  }

  @PostMapping("/payments")
  public Object pay(
      @PathVariable Long storeId,
      @Valid @RequestBody PaymentService.Input r,
      @RequestHeader("Idempotency-Key") String key) {
    return payments.pay(storeId, r, key);
  }
}
