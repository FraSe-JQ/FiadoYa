package pe.fiadoya.shared.application;

import java.time.*;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class OperationalClock {
  private final Environment env;
  private volatile LocalDateTime demonstration;

  public OperationalClock(Environment env) {
    this.env = env;
  }

  public LocalDateTime now() {
    return demonstration == null ? LocalDateTime.now(ZoneId.of("America/Lima")) : demonstration;
  }

  public boolean demo() {
    return Arrays.asList(env.getActiveProfiles()).contains("demo");
  }

  public void set(LocalDateTime time) {
    if (!demo()) throw new IllegalArgumentException("Reloj configurable solo en demostración");
    demonstration = time;
  }
}
