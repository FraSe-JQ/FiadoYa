package pe.fiadoya.reporting.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.Store;

@Component
public class CycleScheduler {
  private final Data data;
  private final CycleService cycles;
  private final OperationalClock clock;

  public CycleScheduler(Data data, CycleService cycles, OperationalClock clock) {
    this.data = data;
    this.cycles = cycles;
    this.clock = clock;
  }

  @Scheduled(cron = "0 * * * * *", zone = "America/Lima")
  @Transactional
  public void close() {
    for (Store s : data.list(Store.class, "where e.active=true")) cycles.close(s.id, clock.now());
  }
}
