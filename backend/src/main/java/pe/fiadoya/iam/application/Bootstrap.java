package pe.fiadoya.iam.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Component
public class Bootstrap implements ApplicationRunner {
  private final Data data;
  private final BCryptPasswordEncoder p;
  private final String email, password;

  public Bootstrap(
      Data data,
      BCryptPasswordEncoder p,
      @Value("${app.bootstrap-email}") String email,
      @Value("${app.bootstrap-password}") String password) {
    this.data = data;
    this.p = p;
    this.email = email;
    this.password = password;
  }

  @Transactional
  public void run(ApplicationArguments args) {
    if (email.isBlank() || !data.list(User.class, "where e.role='PLATFORM_ADMIN'").isEmpty())
      return;
    if (password.length() < 12 || password.length() > 72)
      throw new IllegalArgumentException("Bootstrap requiere contraseña de 12–72 caracteres");
    User u = new User();
    u.email = email.toLowerCase();
    u.name = "Administrador de plataforma";
    u.role = "PLATFORM_ADMIN";
    u.passwordHash = p.encode(password);
    data.save(u);
  }
}
