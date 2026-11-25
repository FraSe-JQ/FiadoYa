package pe.fiadoya.sales.api;

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
public class SalesController {
  private final Data data;
  private final Access access;
  private final SalesService sales;
  private final PaymentService payments;
  private final CreditService credit;
  private final OperationalClock clock;

  public SalesController(
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

  @PostMapping("/sales/simulate")
  public Object simulate(@PathVariable Long storeId, @Valid @RequestBody SalesService.Sale r) {
    return sales.simulate(storeId, r);
  }

  @PostMapping("/sales")
  public Object purchase(
      @PathVariable Long storeId,
      @Valid @RequestBody SalesService.Sale r,
      @RequestHeader("Idempotency-Key") String key) {
    return sales.purchase(storeId, r, key);
  }

  @GetMapping("/purchases/{id}")
  public Object purchase(@PathVariable Long storeId, @PathVariable Long id) {
    Purchase p = data.get(Purchase.class, id);
    access.account(storeId, p.accountId, false);
    if (!p.storeId.equals(storeId))
      throw new org.springframework.security.access.AccessDeniedException("Compra ajena");
    return Map.of(
        "purchase",
        p,
        "terms",
        sales.snapshot(p),
        "items",
        data.list(PurchaseItem.class, "where e.purchaseId=?1", id),
        "schedule",
        data.list(Obligation.class, "where e.purchaseId=?1 order by e.number", id));
  }
}
