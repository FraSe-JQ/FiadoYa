package pe.fiadoya.iam.api;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final Data data;
  private final BCryptPasswordEncoder passwords;
  private final JwtEncoder encoder;

  public AuthController(Data data, BCryptPasswordEncoder passwords, JwtEncoder encoder) {
    this.data = data;
    this.passwords = passwords;
    this.encoder = encoder;
  }

  public record Registration(
      @Email @NotBlank String email,
      @Size(min = 10, max = 72) @NotNull String password,
      @NotBlank @Size(max = 100) String name) {}

  public record Login(@Email @NotBlank String email, @NotBlank @Size(max = 72) String password) {}

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("token", token.getToken());
  }

  @PostMapping("/register")
  @Transactional
  public Object register(@Valid @RequestBody Registration r) {
    User u = new User();
    u.email = r.email().trim().toLowerCase(Locale.ROOT);
    u.name = r.name().trim();
    u.passwordHash = passwords.encode(r.password());
    u.role = "CUSTOMER";
    data.save(u);
    return view(u);
  }

  @PostMapping("/login")
  @Transactional
  public Object login(
      @Valid @RequestBody Login r, HttpServletResponse response, HttpServletRequest request) {
    User u =
        data.first(User.class, "where e.email=?1", r.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));
    if (!u.active || !passwords.matches(r.password(), u.passwordHash))
      throw new IllegalArgumentException("Usuario o contraseña incorrectos");
    Session s = new Session();
    s.userId = u.id;
    s.tokenId = UUID.randomUUID().toString();
    s.expiresAt = Instant.now().plusSeconds(3600);
    data.save(s);
    var claims =
        JwtClaimsSet.builder()
            .issuer("fiadoya")
            .subject(u.id.toString())
            .id(s.tokenId)
            .issuedAt(Instant.now())
            .expiresAt(s.expiresAt)
            .build();
    String jwt =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    response.addHeader(
        "Set-Cookie",
        org.springframework.http.ResponseCookie.from("FIADOYA_SESSION", jwt)
            .httpOnly(true)
            .secure(request.isSecure())
            .sameSite("Strict")
            .path("/")
            .maxAge(3600)
            .build()
            .toString());
    Audit audit = new Audit();
    audit.actorId = u.id;
    audit.action = "LOGIN";
    audit.resource = "session";
    audit.occurredAt = Instant.now();
    data.save(audit);
    return sessionView(u);
  }

  @GetMapping("/me")
  public Object me(Authentication access) {
    return sessionView((User) access.getPrincipal());
  }

  @PostMapping("/logout")
  @Transactional
  public void logout(
      Authentication access, HttpServletRequest request, HttpServletResponse response) {
    // Revoke all active sessions for this user; no usable token survives logout.
    User u = (User) access.getPrincipal();
    for (Session s : data.list(Session.class, "where e.userId=?1 and e.revoked=false", u.id))
      s.revoked = true;
    response.addHeader(
        "Set-Cookie", "FIADOYA_SESSION=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
  }

  public static Map<String, Object> view(User u) {
    return Map.of("id", u.id, "email", u.email, "name", u.name, "role", u.role);
  }

  private Map<String, Object> sessionView(User user) {
    Map<String, Object> result = new LinkedHashMap<>(view(user));
    result.put(
        "managedStoreIds",
        data.list(Membership.class, "where e.userId=?1", user.id).stream()
            .map(membership -> membership.storeId)
            .toList());
    return result;
  }
}
