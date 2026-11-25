import { ChangeDetectorRef } from "@angular/core";
import { Component, input, inject, effect } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { ApiService } from "../core/api.service";
import { FieldComponent } from "../shared/field.component";
@Component({
  selector: "fy-account",
  standalone: true,
  imports: [FormsModule, CommonModule, FieldComponent],
  template: ` <div class="section-heading">
      <div>
        <h2>Cuenta corriente</h2>
        <p>Capital pendiente, obligaciones y movimientos del cliente.</p>
      </div>
      @if (detail) {
        <a
          class="button secondary"
          [href]="
            '/api/stores/' +
            storeId() +
            '/accounts/' +
            accountId +
            '/export.csv'
          "
          >Exportar CSV</a
        >
      }
    </div>
    <fy-field
      label="Cuenta del cliente"
      help="Selecciona una cuenta autorizada del comercio. Ejemplo: Juan Pérez. Cada cuenta tiene una moneda y condiciones independientes."
      ><label class="sr-only" for="account-select">Cuenta del cliente</label
      ><select
        id="account-select"
        [(ngModel)]="accountId"
        (ngModelChange)="load()"
      >
        <option [ngValue]="null">Seleccionar cliente</option>
        @for (entry of customers(); track entry.account.id) {
          <option [ngValue]="entry.account.id">
            {{ entry.customer.firstName }} {{ entry.customer.lastName }} ·
            {{ entry.account.currency }}
          </option>
        }
      </select></fy-field
    >
    @if (error) {
      <p class="alert error" role="alert">{{ error }}</p>
    }
    @if (message) {
      <p class="alert success" role="status">{{ message }}</p>
    }
    @if (busy) {
      <p class="loading" role="status">Actualizando cuenta…</p>
    }
    @if (detail) {
      <div class="account-heading">
        <div class="avatar">
          {{ detail.customer.firstName.slice(0, 1)
          }}{{ detail.customer.lastName.slice(0, 1) }}
        </div>
        <div>
          <h3>
            {{ detail.customer.firstName }} {{ detail.customer.lastName }}
          </h3>
          <p>
            {{ detail.customer.documentType }}
            {{ detail.customer.documentNumber }} · {{ detail.account.currency }}
          </p>
        </div>
        <span class="tag">{{
          detail.customer.active ? "Cliente activo" : "Cliente inactivo"
        }}</span>
      </div>
      <div class="metrics">
        <article>
          <span>Capital pendiente</span
          ><strong>{{ money(detail.account.used) }}</strong
          ><small>Incluye gracia capitalizada</small>
        </article>
        <article>
          <span>Crédito disponible</span
          ><strong class="green">{{ money(detail.available) }}</strong
          ><small>Límite {{ money(detail.terms.creditLimit) }}</small>
        </article>
        <article>
          <span>Interés compensatorio</span
          ><strong>{{ money(detail.interest) }}</strong
          ><small>De las obligaciones pendientes</small>
        </article>
        <article>
          <span>Interés moratorio</span
          ><strong class="red">{{ money(detail.penalty) }}</strong
          ><small>Solo sobre obligaciones vencidas</small>
        </article>
      </div>
      @if (!readOnly()) {
        <section class="panel settlement">
          <div>
            <h3>Liquidación de obligaciones exigibles</h3>
            <p>
              {{ quote?.obligations.length ?? 0 }} obligaciones · Mora, interés
              compensatorio y capital.
            </p>
            <strong class="settlement-total">{{
              money(quote?.total ?? 0)
            }}</strong>
          </div>
          <div class="settlement-controls">
            <fy-field
              label="Medio de pago"
              help="Forma de recepción del importe. Ejemplo: efectivo. No procesa cobros bancarios; registra la recepción verificada."
              ><label class="sr-only" for="payment-method">Medio de pago</label
              ><select id="payment-method" [(ngModel)]="method">
                <option value="CASH">Efectivo</option>
                <option value="TRANSFER">Transferencia</option>
                <option value="CARD">Tarjeta</option>
              </select></fy-field
            ><button
              type="button"
              [disabled]="busy || !quote?.total"
              (click)="confirming = true"
            >
              Registrar pago completo
            </button>
          </div>
        </section>
      }
      @if (confirming) {
        <section
          class="alert confirmation"
          role="alertdialog"
          aria-label="Confirmar pago"
        >
          <h3>Confirmar recepción de {{ money(quote.total) }}</h3>
          <p>
            Se registrará la cancelación completa de las obligaciones exigibles.
            Verifica que hayas recibido el dinero.
          </p>
          <div class="form-actions">
            <button
              class="secondary"
              type="button"
              (click)="confirming = false"
            >
              Cancelar</button
            ><button type="button" [disabled]="busy" (click)="pay()">
              Confirmar pago recibido
            </button>
          </div>
        </section>
      }
      <section class="panel">
        <h3>Obligaciones y cronogramas</h3>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Compra / cuota</th>
                <th>Vencimiento</th>
                <th class="numeric">Saldo inicial</th>
                <th class="numeric">Capital</th>
                <th class="numeric">Interés</th>
                <th class="numeric">Importe</th>
                <th class="numeric">Saldo final</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              @for (row of detail.obligations; track row.id) {
                <tr>
                  <td>#{{ row.purchaseId }} / {{ row.number }}</td>
                  <td>{{ row.dueDate | date: "dd/MM/yyyy" }}</td>
                  <td class="numeric">{{ money(row.opening) }}</td>
                  <td class="numeric">{{ money(row.principal) }}</td>
                  <td class="numeric">{{ money(row.interest) }}</td>
                  <td class="numeric">
                    <strong>{{ money(row.amount) }}</strong>
                  </td>
                  <td class="numeric">{{ money(row.closing) }}</td>
                  <td>
                    <span
                      class="tag"
                      [class.danger]="!row.paid && isOverdue(row)"
                      >{{
                        row.paid
                          ? "Pagada"
                          : isOverdue(row)
                            ? "Vencida"
                            : "Pendiente"
                      }}</span
                    >
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="8" class="empty">
                    Todavía no hay compras en esta cuenta.
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </section>
      <div class="two-columns">
        <section class="panel">
          <h3>Compras registradas</h3>
          @for (purchase of detail.purchases; track purchase.id) {
            <div class="movement">
              <div>
                <strong>Compra #{{ purchase.id }}</strong
                ><small
                  >{{ purchase.purchasedAt | date: "dd/MM/yyyy HH:mm" }} ·
                  {{
                    purchase.mode === "INSTALLMENTS"
                      ? purchase.months + " cuotas"
                      : "Fin de mes"
                  }}</small
                ><small
                  >Periodo cero:
                  {{ purchase.periodZero | date: "dd/MM/yyyy" }}</small
                >
              </div>
              <strong>{{ money(purchase.financed) }}</strong>
            </div>
          } @empty {
            <p class="empty">Sin compras registradas.</p>
          }
        </section>
        <section class="panel">
          <h3>Pagos recibidos</h3>
          @for (payment of detail.payments; track payment.id) {
            <div class="movement">
              <div>
                <strong>Pago #{{ payment.id }}</strong
                ><small
                  >{{ payment.paidAt | date: "dd/MM/yyyy" }} ·
                  {{ paymentMethod(payment.method) }}</small
                ><small
                  >Capital {{ money(payment.principal) }} · Mora
                  {{ money(payment.penalty) }}</small
                >
              </div>
              <strong class="green">{{ money(payment.received) }}</strong>
            </div>
          } @empty {
            <p class="empty">Sin pagos registrados.</p>
          }
        </section>
      </div>
      <section class="panel">
        <h3>Estados de cuenta históricos</h3>
        @for (cycle of detail.cycles; track cycle.id) {
          <details class="cycle">
            <summary>
              Corte {{ cycle.cutoff | date: "dd/MM/yyyy HH:mm" }}
            </summary>
            <p>
              Generado el {{ cycle.generatedAt | date: "dd/MM/yyyy HH:mm" }}.
              Los importes y estados se conservan como estaban al generar este
              cierre.
            </p>
            <div class="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Compra / cuota</th>
                    <th>Vencimiento</th>
                    <th class="numeric">Capital</th>
                    <th class="numeric">Interés</th>
                    <th class="numeric">Importe</th>
                    <th>Estado al cierre</th>
                  </tr>
                </thead>
                <tbody>
                  @for (
                    obligation of cycleSnapshot(cycle).obligations;
                    track obligation.id
                  ) {
                    <tr>
                      <td>
                        #{{ obligation.purchaseId }} / {{ obligation.number }}
                      </td>
                      <td>{{ obligation.dueDate | date: "dd/MM/yyyy" }}</td>
                      <td class="numeric">{{ money(obligation.principal) }}</td>
                      <td class="numeric">{{ money(obligation.interest) }}</td>
                      <td class="numeric">{{ money(obligation.amount) }}</td>
                      <td>{{ obligation.paid ? "Pagada" : "Pendiente" }}</td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="6" class="empty">
                        Sin obligaciones en el periodo de este cierre.
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <h4 class="spaced">Compras incluidas</h4>
            @for (
              purchase of cycleSnapshot(cycle).purchases;
              track purchase.id
            ) {
              <div class="movement">
                <div>
                  <strong>Compra #{{ purchase.id }}</strong
                  ><small
                    >{{ purchase.purchasedAt | date: "dd/MM/yyyy HH:mm" }} ·
                    {{
                      purchase.mode === "INSTALLMENTS"
                        ? purchase.months + " cuotas"
                        : "Fin de mes"
                    }}</small
                  >
                </div>
                <strong>{{ money(purchase.financed) }}</strong>
              </div>
            } @empty {
              <p>Sin compras incluidas.</p>
            }
          </details>
        } @empty {
          <p class="empty">Los estados se generan al cierre de cada ciclo.</p>
        }
      </section>
    }`,
})
export class AccountComponent {
  private changeDetector = inject(ChangeDetectorRef);
  private api = inject(ApiService);
  storeId = input.required<number>();
  customers = input.required<any[]>();
  readOnly = input(false);
  accountId: number | null = null;
  detail: any = null;
  quote: any = null;
  busy = false;
  error = "";
  message = "";
  method = "CASH";
  confirming = false;
  today = "";
  private operationKey = "";
  constructor() {
    effect(() => {
      const storeId = this.storeId();
      const entries = this.customers().filter(
        (entry) => entry.account.storeId === storeId,
      );
      if (!entries.length) {
        this.accountId = null;
        this.detail = null;
        this.quote = null;
        return;
      }
      if (!entries.some((entry) => entry.account.id === this.accountId)) {
        this.accountId = entries[0].account.id;
        this.load();
      }
    });
  }
  isOverdue(row: any) {
    return row.dueDate < this.today;
  }
  paymentMethod(code: string) {
    return (
      (
        {
          CASH: "Efectivo",
          TRANSFER: "Transferencia",
          CARD: "Tarjeta",
        } as Record<string, string>
      )[code] ?? "Otro medio"
    );
  }
  cycleSnapshot(cycle: any): { purchases: any[]; obligations: any[] } {
    if (!cycle.view) {
      try {
        cycle.view = JSON.parse(cycle.snapshotJson);
      } catch {
        cycle.view = { purchases: [], obligations: [] };
      }
    }
    return cycle.view;
  }
  money(value: any) {
    return new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: this.detail?.account.currency ?? "PEN",
    }).format(Number(value));
  }
  async load() {
    try {
      if (!this.accountId) return;
      this.busy = true;
      this.error = "";
      try {
        [this.detail, this.quote] = await Promise.all([
          this.api.get(`/stores/${this.storeId()}/accounts/${this.accountId}`),
          this.api.get(
            `/stores/${this.storeId()}/accounts/${this.accountId}/quote`,
          ),
        ]);
        this.today = (await this.api.get("/clock")).dateTime.substring(0, 10);
        this.operationKey = crypto.randomUUID();
      } catch (error: any) {
        this.error = error.error?.message ?? "No se pudo consultar la cuenta.";
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async pay() {
    try {
      this.busy = true;
      this.error = "";
      try {
        const result = await this.api.post(
          `/stores/${this.storeId()}/payments`,
          {
            accountId: this.accountId,
            received: Number(this.quote.total),
            method: this.method,
          },
          this.operationKey,
        );
        this.confirming = false;
        this.message = `Pago #${result.id} registrado. Capital pendiente: ${this.money(result.remainingCapital)}.`;
        await this.load();
      } catch (error: any) {
        this.error =
          error.error?.message ??
          "No se pudo registrar. Reintenta para verificar la misma operación.";
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
}
