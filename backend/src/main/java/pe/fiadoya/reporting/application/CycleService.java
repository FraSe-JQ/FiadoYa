package pe.fiadoya.reporting.application;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fiadoya.accounts.application.CreditService;
import pe.fiadoya.finance.domain.FinanceEngine;
import pe.fiadoya.sales.application.SalesService;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Service
public class CycleService {
  private final Data data;
  private final OperationalClock clock;
  private final SalesService sales;
  private final CreditService credit;

  public CycleService(Data data, OperationalClock clock, SalesService sales, CreditService credit) {
    this.data = data;
    this.clock = clock;
    this.sales = sales;
    this.credit = credit;
  }

  @Transactional
  public int close(Long storeId, LocalDateTime asOf) {
    int count = 0;
    for (Account account : data.list(Account.class, "where e.storeId=?1 order by e.id", storeId)) {
      data.lock(Account.class, account.id);
      Set<LocalDateTime> cuts = new TreeSet<>();
      List<Purchase> accountPurchases =
          data.list(Purchase.class, "where e.accountId=?1 order by e.purchasedAt,e.id", account.id);
      for (Purchase purchase : accountPurchases)
        if (!purchase.cutoff.isAfter(asOf)) cuts.add(purchase.cutoff);
      var terms = credit.terms(credit.latest(account.id));
      // Every elapsed month needs a statement, even when it contains only installments
      // from an older purchase and no new sales.
      if (!cuts.isEmpty()) {
        YearMonth firstMonth = YearMonth.from(cuts.iterator().next());
        for (YearMonth month = firstMonth;
            !month.isAfter(YearMonth.from(asOf));
            month = month.plusMonths(1)) {
          LocalDateTime monthlyCutoff =
              FinanceEngine.day(month, terms.cutoffDay()).atTime(terms.cutoffTime());
          if (!monthlyCutoff.isAfter(asOf)) cuts.add(monthlyCutoff);
        }
      }
      LocalDateTime current =
          FinanceEngine.day(YearMonth.from(asOf), terms.cutoffDay()).atTime(terms.cutoffTime());
      if (!current.isAfter(asOf)) cuts.add(current);
      for (LocalDateTime cutoff : cuts)
        if (data.first(
                BillingCycle.class, "where e.accountId=?1 and e.cutoff=?2", account.id, cutoff)
            .isEmpty()) {
          LocalDate nextCutoff =
              FinanceEngine.day(YearMonth.from(cutoff).plusMonths(1), terms.cutoffDay());
          var obligations =
              data
                  .list(
                      Obligation.class, "where e.accountId=?1 order by e.dueDate,e.id", account.id)
                  .stream()
                  .filter(
                      obligation ->
                          !obligation.dueDate.isBefore(cutoff.toLocalDate())
                              && obligation.dueDate.isBefore(nextCutoff))
                  .toList();
          Set<Long> purchaseIds = new HashSet<>();
          obligations.forEach(obligation -> purchaseIds.add(obligation.purchaseId));
          var purchases =
              accountPurchases.stream()
                  .filter(
                      purchase ->
                          purchase.cutoff.equals(cutoff) || purchaseIds.contains(purchase.id))
                  .toList();
          BillingCycle cycle = new BillingCycle();
          cycle.storeId = storeId;
          cycle.accountId = account.id;
          cycle.cutoff = cutoff;
          cycle.generatedAt = Instant.now();
          cycle.snapshotJson =
              sales.serialize(Map.of("purchases", purchases, "obligations", obligations));
          data.save(cycle);
          count++;
        }
    }
    return count;
  }
}
