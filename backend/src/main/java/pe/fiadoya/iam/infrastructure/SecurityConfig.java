package pe.fiadoya.iam.infrastructure;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.*;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Configuration
public class SecurityConfig {
  @Bean
  BCryptPasswordEncoder passwords() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecretKeySpec signingKey(@Value("${app.jwt-secret}") String secret) {
    if (secret.length() < 32)
      throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
    return new SecretKeySpec(
        secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
  }

  @Bean
  JwtEncoder encoder(SecretKeySpec key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  JwtDecoder decoder(SecretKeySpec key) {
    return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http, Data data, @Value("${app.cors-origin}") String origin) throws Exception {
    var csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
    var handler = new CsrfTokenRequestAttributeHandler();
    http.cors(
            c ->
                c.configurationSource(
                    r -> {
                      var x = new CorsConfiguration();
                      x.setAllowedOrigins(List.of(origin));
                      x.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                      x.setAllowedHeaders(
                          List.of(
                              "Content-Type", "X-XSRF-TOKEN", "Authorization", "Idempotency-Key"));
                      x.setAllowCredentials(true);
                      return x;
                    }))
        .csrf(c -> c.csrfTokenRepository(csrf).csrfTokenRequestHandler(handler))
        .sessionManagement(
            s ->
                s.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            access ->
                access
                    .requestMatchers(
                        "/api/auth/csrf",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/actuator/health",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.bearerTokenResolver(
                        r -> {
                          String access = r.getHeader("Authorization");
                          if (access != null && access.startsWith("Bearer "))
                            return access.substring(7);
                          if (r.getCookies() != null)
                            for (var cookie : r.getCookies())
                              if (cookie.getName().equals("FIADOYA_SESSION"))
                                return cookie.getValue();
                          return null;
                        })
                    .jwt(
                        j ->
                            j.jwtAuthenticationConverter(
                                jwt -> {
                                  var s =
                                      data.first(
                                              Session.class,
                                              "where e.tokenId=?1 and e.revoked=false",
                                              jwt.getId())
                                          .orElseThrow(
                                              () ->
                                                  new org.springframework.security.oauth2.core
                                                      .OAuth2AuthenticationException(
                                                      "Sesión revocada"));
                                  User user = data.get(User.class, s.userId);
                                  if (!user.active || s.expiresAt.isBefore(Instant.now()))
                                    throw new org.springframework.security.oauth2.core
                                        .OAuth2AuthenticationException("Sesión expirada");
                                  return new UsernamePasswordAuthenticationToken(
                                      user,
                                      null,
                                      List.of(new SimpleGrantedAuthority("ROLE_" + user.role)));
                                })));
    http.headers(
        h ->
            h.contentSecurityPolicy(
                c ->
                    c.policyDirectives(
                        "default-src 'self'; img-src 'self' https: data:; style-src 'self'"
                            + " 'unsafe-inline'; frame-ancestors 'none'")));
    return http.build();
  }
}
