import { ChangeDetectorRef } from "@angular/core";
import { Component, inject, output } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../core/api.service";
import { FieldComponent } from "../shared/field.component";
@Component({
  selector: "fy-auth",
  standalone: true,
  imports: [FormsModule, FieldComponent],
  template: ` <main class="auth-layout">
    <section class="auth-story">
      <a class="brand" href="/">Fiado<span>Ya</span><b>F</b></a>
      <div>
        <p class="eyebrow">El crédito de tu comercio, en orden</p>
        <h1>La confianza de siempre.<br />Las cuentas, claras.</h1>
        <p>
          Compras, cuotas y pagos en un solo lugar. Conserva cada acuerdo y
          conoce cuánto está pendiente por cobrar.
        </p>
        <div class="auth-note">
          SI642 · Finanzas e Ingeniería Económica<br />UPC · Grupo 4 · 2026-20
        </div>
      </div>
      <small>Hecho para pequeños comercios peruanos.</small>
    </section>
    <section class="auth-form">
      <div class="auth-box">
        <span class="tag">FiadoYa</span>
        <h2>{{ register ? "Crea tu cuenta" : "Bienvenido de nuevo" }}</h2>
        <p class="muted">
          {{
            register
              ? "Tu registro habilita un perfil de cliente. La plataforma asigna los administradores."
              : "Ingresa para consultar y administrar tus cuentas."
          }}
        </p>
        @if (error) {
          <p class="alert error" role="alert">{{ error }}</p>
        }
        <form (ngSubmit)="submit()">
          @if (register) {
            <fy-field
              label="Nombre completo"
              help="Nombre que identifica tu cuenta. Ejemplo: Ana Torres. Obligatorio, hasta 100 caracteres."
              ><label class="sr-only" for="auth-name">Nombre completo</label
              ><input
                id="auth-name"
                name="name"
                [(ngModel)]="name"
                required
                maxlength="100"
                autocomplete="name"
            /></fy-field>
          }
          <fy-field
            label="Correo electrónico"
            help="Correo utilizado para ingresar. Ejemplo: ana@example.test. Formato válido, sin espacios."
            ><label class="sr-only" for="auth-email">Correo electrónico</label
            ><input
              id="auth-email"
              name="email"
              type="email"
              [(ngModel)]="email"
              required
              autocomplete="username"
          /></fy-field>
          <fy-field
            label="Contraseña"
            help="Protege tu cuenta. Utiliza 10–72 caracteres para registrarte; ejemplo de formato: varias palabras y números. No compartas tu contraseña."
            ><label class="sr-only" for="auth-password">Contraseña</label
            ><input
              id="auth-password"
              name="password"
              type="password"
              [(ngModel)]="password"
              required
              [minlength]="register ? 10 : 1"
              maxlength="72"
              [autocomplete]="register ? 'new-password' : 'current-password'"
          /></fy-field>
          <button class="full" type="submit" [disabled]="busy">
            {{
              busy
                ? "Procesando…"
                : register
                  ? "Registrar usuario"
                  : "Iniciar sesión"
            }}
          </button>
        </form>
        <p class="auth-toggle">
          {{ register ? "¿Ya tienes una cuenta?" : "¿Primera vez aquí?" }}
          <button
            type="button"
            class="text-button"
            (click)="register = !register; error = ''"
          >
            {{ register ? "Inicia sesión" : "Regístrate" }}
          </button>
        </p>
        <p class="fine">
          La información de cada establecimiento se mantiene separada. Tu sesión
          expira después de una hora.
        </p>
      </div>
    </section>
  </main>`,
})
export class AuthComponent {
  private changeDetector = inject(ChangeDetectorRef);
  private api = inject(ApiService);
  authenticated = output<any>();
  register = false;
  name = "";
  email = "";
  password = "";
  busy = false;
  error = "";
  async submit() {
    try {
      this.busy = true;
      this.error = "";
      try {
        await this.api.get("/auth/csrf");
        if (this.register)
          await this.api.post("/auth/register", {
            email: this.email,
            password: this.password,
            name: this.name,
          });
        const user = await this.api.post("/auth/login", {
          email: this.email,
          password: this.password,
        });
        this.password = "";
        this.authenticated.emit(user);
      } catch (error: any) {
        this.error =
          error.error?.message ??
          "No se pudo conectar. Revisa tu conexión e inténtalo de nuevo.";
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
}
