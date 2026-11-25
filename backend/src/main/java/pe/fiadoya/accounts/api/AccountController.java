package pe.fiadoya.accounts.api;

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
public class AccountController {
  private final Data data;
  private final Access access;
  private final SalesService sales;
  private final PaymentService payments;
  private final CreditService credit;
  private final OperationalClock clock;

  public AccountController(
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

  @GetMapping("/accounts/{id}")
  public Object account(@PathVariable Long storeId, @PathVariable Long id) {
    Account account = access.account(storeId, id, false);
    var terms = credit.terms(credit.latest(id));
    var obligations =
        data.list(Obligation.class, "where e.accountId=?1 order by e.dueDate,e.id", id);
    BigDecimal interest = BigDecimal.ZERO, penalty = BigDecimal.ZERO, overdue = BigDecimal.ZERO;
    for (Obligation o : obligations)
      if (!o.paid) {
        interest = interest.add(o.interest);
        BigDecimal mora = payments.penalty(o);
        penalty = penalty.add(mora);
        if (o.dueDate.isBefore(clock.now().toLocalDate()))
          overdue = overdue.add(o.amount).add(mora);
      }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("account", account);
    result.put("customer", data.get(Customer.class, account.customerId));
    result.put("terms", terms);
    result.put("available", terms.creditLimit().subtract(account.used));
    result.put("interest", interest);
    result.put("penalty", penalty);
    result.put("overdue", overdue);
    result.put("obligations", obligations);
    result.put(
        "purchases",
        data.list(Purchase.class, "where e.accountId=?1 order by e.purchasedAt,e.id", id));
    result.put(
        "payments", data.list(Payment.class, "where e.accountId=?1 order by e.paidAt,e.id", id));
    result.put(
        "cycles", data.list(BillingCycle.class, "where e.accountId=?1 order by e.cutoff desc", id));
    return result;
  }
}
