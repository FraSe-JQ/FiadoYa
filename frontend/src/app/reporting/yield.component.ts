import { ChangeDetectorRef } from "@angular/core";
import { Component, input, inject } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { ApiService } from "../core/api.service";
import { FieldComponent } from "../shared/field.component";
@Component({
  selector: "fy-yield",
  standalone: true,
  imports: [CommonModule, FormsModule, FieldComponent],
  template: ` <section class="panel spaced">
    <h2>Rendimiento efectivo del financiamiento</h2>
    <p>
      Compara el capital entregado con las cuotas contratadas y sus fechas. Esta
      medida excluye costos del negocio y mora; no representa utilidad.
    </p>
    <div class="form-grid">
      <fy-field
        label="Cliente del reporte"
        help="Cuenta cuyas compras se analizarán. Ejemplo: Juan Pérez. Solo cuentas autorizadas en el establecimiento."
        ><label class="sr-only" for="yield-customer">Cliente del reporte</label
        ><select
          id="yield-customer"
          [(ngModel)]="accountId"
          (ngModelChange)="loadPurchases()"
        >
          <option [ngValue]="null">Seleccionar cliente</option>
          @for (entry of customers(); track entry.account.id) {
            <option [ngValue]="entry.account.id">
              {{ entry.customer.firstName }} {{ entry.customer.lastName }}
            </option>
          }
        </select></fy-field
      ><fy-field
        label="Compra financiada"
        help="Operación original que se desea analizar. Ejemplo: Compra #1. Se utilizan sus flujos contractuales y fechas con base 30/360."
        ><label class="sr-only" for="yield-purchase">Compra financiada</label
        ><select
          id="yield-purchase"
          [(ngModel)]="purchaseId"
          (ngModelChange)="calculate()"
        >
          <option [ngValue]="null">Seleccionar compra</option>
          @for (purchase of purchases; track purchase.id) {
            <option [ngValue]="purchase.id">
              #{{ purchase.id }} ·
              {{ purchase.purchasedAt | date: "dd/MM/yyyy" }} ·
              {{ purchase.currency }} {{ purchase.financed | number: "1.2-2" }}
            </option>
          }
        </select></fy-field
      >
    </div>
    @if (error) {
      <p class="alert error" role="alert">{{ error }}</p>
    }
    @if (result) {
      <div class="total-line">
        <span>Rendimiento anual efectivo contractual</span
        ><strong
          >{{ result.annualEffectiveYield * 100 | number: "1.7-7" }}%</strong
        >
      </div>
      <div class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Fecha del cobro</th>
              <th class="numeric">Flujo contratado</th>
            </tr>
          </thead>
          <tbody>
            @for (flow of result.flows; track $index) {
              <tr>
                <td>{{ flow.date | date: "dd/MM/yyyy" }}</td>
                <td class="numeric">{{ flow.amount | number: "1.2-2" }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
    @if (accountId) {
      <div class="form-actions">
        <a
          class="button secondary"
          [href]="
            '/api/stores/' +
            storeId() +
            '/accounts/' +
            accountId +
            '/export.csv'
          "
          >Exportar cuenta en CSV</a
        >
      </div>
    }
  </section>`,
})
export class YieldComponent {
  private changeDetector = inject(ChangeDetectorRef);
  private api = inject(ApiService);
  storeId = input.required<number>();
  customers = input.required<any[]>();
  accountId: number | null = null;
  purchaseId: number | null = null;
  purchases: any[] = [];
  result: any = null;
  error = "";
  async loadPurchases() {
    try {
      this.result = null;
      this.purchaseId = null;
      try {
        this.purchases = (
          await this.api.get(
            `/stores/${this.storeId()}/accounts/${this.accountId}`,
          )
        ).purchases;
      } catch (error: any) {
        this.error =
          error.error?.message ?? "No se pudieron consultar las compras.";
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async calculate() {
    try {
      this.error = "";
      if (!this.purchaseId) return;
      try {
        this.result = await this.api.get(
          `/stores/${this.storeId()}/purchases/${this.purchaseId}/yield`,
        );
      } catch (error: any) {
        this.error =
          error.error?.message ?? "No se pudo calcular el rendimiento.";
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
}
