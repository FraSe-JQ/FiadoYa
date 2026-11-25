package pe.fiadoya.reporting.api;

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
public class DashboardController {
  private final Data data;
  private final Access access;
  private final SalesService sales;
  private final PaymentService payments;
  private final CreditService credit;
  private final OperationalClock clock;

  public DashboardController(
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

  @GetMapping("/dashboard")
  public Object dashboard(@PathVariable Long storeId) {
    access.store(storeId, true);
    BigDecimal capital = BigDecimal.ZERO,
        interest = BigDecimal.ZERO,
        penalty = BigDecimal.ZERO,
        overdue = BigDecimal.ZERO,
        collected = BigDecimal.ZERO;
    List<Obligation> next = new ArrayList<>();
    Map<String, BigDecimal> projection = new TreeMap<>();
    // Currency totals remain separate; dashboard selects PEN by default, USD through report filter.
    Map<String, Object> currencies = new LinkedHashMap<>();
    for (String currency : List.of("PEN", "USD")) {
      capital = BigDecimal.ZERO;
      interest = BigDecimal.ZERO;
      penalty = BigDecimal.ZERO;
      overdue = BigDecimal.ZERO;
      collected = BigDecimal.ZERO;
      next = new ArrayList<>();
      projection = new TreeMap<>();
      Set<Long> ids = new HashSet<>();
      for (Account account :
          data.list(Account.class, "where e.storeId=?1 and e.currency=?2", storeId, currency)) {
        capital = capital.add(account.used);
        ids.add(account.id);
      }
      for (Obligation o :
          data.list(
              Obligation.class,
              "where e.storeId=?1 and e.paid=false order by e.dueDate,e.id",
              storeId))
        if (ids.contains(o.accountId)) {
          interest = interest.add(o.interest);
          BigDecimal m = payments.penalty(o);
          penalty = penalty.add(m);
          if (o.dueDate.isBefore(clock.now().toLocalDate())) overdue = overdue.add(o.amount).add(m);
          else next.add(o);
          projection.merge(o.dueDate.toString().substring(0, 7), o.amount, BigDecimal::add);
        }
      for (Payment p : data.list(Payment.class, "where e.storeId=?1", storeId))
        if (ids.contains(p.accountId)) collected = collected.add(p.received);
      currencies.put(
          currency,
          Map.of(
              "capital",
              capital,
              "interest",
              interest,
              "penalty",
              penalty,
              "receivables",
              capital.add(interest).add(penalty),
              "overdue",
              overdue,
              "collected",
              collected,
              "upcoming",
              next.stream().limit(8).toList(),
              "projection",
              projection));
    }
    return Map.of(
        "currencies",
        currencies,
        "activeCustomers",
        data.list(Customer.class, "where e.storeId=?1 and e.active=true", storeId).size(),
        "asOf",
        clock.now());
  }
}
