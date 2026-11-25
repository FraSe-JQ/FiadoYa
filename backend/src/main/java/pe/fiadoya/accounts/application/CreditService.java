package pe.fiadoya.accounts.application;

import java.time.Instant;
import org.springframework.stereotype.Service;
import pe.fiadoya.finance.domain.FinanceEngine.*;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Service
public class CreditService {
  private final Data data;

  public CreditService(Data data) {
    this.data = data;
  }

  public CreditTerms latest(Long id) {
    return data.first(CreditTerms.class, "where e.accountId=?1 order by e.id desc", id)
        .orElseThrow(() -> new IllegalArgumentException("Configure las condiciones crediticias"));
  }

  public Terms terms(CreditTerms c) {
    return new Terms(
        c.currency,
        c.creditLimit,
        c.maxMonths,
        c.cutoffDay,
        c.cutoffTime,
        c.paymentDay,
        new Rate(
            c.compensatoryPercent,
            RateType.valueOf(c.compensatoryType),
            c.compensatoryPeriod,
            c.compensatoryCapitalization),
        new Rate(
            c.penaltyPercent,
            RateType.valueOf(c.penaltyType),
            c.penaltyPeriod,
            c.penaltyCapitalization));
  }

  public CreditTerms version(Long id, Terms t) {
    CreditTerms c = new CreditTerms();
    c.accountId = id;
    c.createdAt = Instant.now();
    c.currency = t.currency();
    c.creditLimit = t.creditLimit();
    c.maxMonths = t.maxMonths();
    c.cutoffDay = t.cutoffDay();
    c.cutoffTime = t.cutoffTime();
    c.paymentDay = t.paymentDay();
    c.compensatoryPercent = t.compensatory().percent();
    c.compensatoryType = t.compensatory().type().name();
    c.compensatoryPeriod = t.compensatory().periodDays();
    c.compensatoryCapitalization = t.compensatory().capitalizationDays();
    c.penaltyPercent = t.penalty().percent();
    c.penaltyType = t.penalty().type().name();
    c.penaltyPeriod = t.penalty().periodDays();
    c.penaltyCapitalization = t.penalty().capitalizationDays();
    return data.save(c);
  }
}
