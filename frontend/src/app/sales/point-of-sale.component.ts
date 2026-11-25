import { ChangeDetectorRef } from "@angular/core";
import { Component, input, output, inject, effect } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { ApiService } from "../core/api.service";
import { FieldComponent } from "../shared/field.component";
@Component({
  selector: "fy-pos",
  standalone: true,
  imports: [FormsModule, CommonModule, FieldComponent],
  template: ` <div class="section-heading">
      <div>
        <h2>Nueva venta a crédito</h2>
        <p>
          Selecciona al cliente, agrega productos y revisa el financiamiento.
        </p>
      </div>
    </div>
    @if (error) {
      <p class="alert error" role="alert">{{ error }}</p>
    }
    @if (success) {
      <p class="alert success" role="status">{{ success }}</p>
    }
    <div class="pos-layout">
      <section class="panel">
        <h3>Cliente y productos</h3>
        <fy-field
          label="Cliente"
          help="Cuenta que financiará la compra. Ejemplo: Juan Pérez. Solo clientes activos del comercio; su moneda debe coincidir con los productos."
          ><label class="sr-only" for="sale-customer">Cliente</label
          ><select
            id="sale-customer"
            [(ngModel)]="accountId"
            (ngModelChange)="invalidate()"
          >
            <option [ngValue]="null">Seleccionar cliente</option>
            @for (entry of customers(); track entry.account.id) {
              @if (entry.customer.active) {
                <option [ngValue]="entry.account.id">
                  {{ entry.customer.firstName }} {{ entry.customer.lastName }} ·
                  {{ entry.account.currency }}
                </option>
              }
            }
          </select></fy-field
        >
        @if (selected()) {
          <div class="credit-strip">
            <div>
              <span>Límite</span
              ><strong>{{ money(selected().terms.creditLimit) }}</strong>
            </div>
            <div>
              <span>Utilizado</span
              ><strong>{{ money(selected().account.used) }}</strong>
            </div>
            <div>
              <span>Disponible</span
              ><strong class="green">{{
                money(selected().terms.creditLimit - selected().account.used)
              }}</strong>
            </div>
          </div>
          <p class="fine">
            Tasa {{ selected().terms.compensatory.percent }}% ·
            {{
              selected().terms.compensatory.type === "NOMINAL"
                ? "nominal"
                : "efectiva"
            }}
            / {{ selected().terms.compensatory.periodDays }} días · Máximo
            {{ selected().terms.maxMonths }} cuotas
          </p>
        }
        <fy-field
          label="Buscar en catálogo"
          help="Filtra por nombre o código. Ejemplo: arroz. Búsqueda local sobre productos activos del comercio."
          ><label class="sr-only" for="sale-search">Buscar en catálogo</label
          ><input
            id="sale-search"
            [(ngModel)]="query"
            placeholder="Nombre o código del producto"
        /></fy-field>
        <div class="catalog-list">
          @for (product of filtered(); track product.id) {
            <button class="catalog-item" type="button" (click)="add(product)">
              <span class="product-initial">{{ product.name.slice(0, 1) }}</span
              ><span
                ><strong>{{ product.name }}</strong
                ><small>{{ product.code }} · {{ product.unit }}</small></span
              ><b>{{ money(product.cashPrice, product.currency) }}</b
              ><span class="add-symbol" aria-hidden="true">+</span>
            </button>
          } @empty {
            <p class="empty">No hay productos activos que coincidan.</p>
          }
        </div>
      </section>
      <section class="panel">
        <h3>Detalle de la compra</h3>
        @for (item of cart; track item.product.id) {
          <div class="cart-row">
            <div>
              <strong>{{ item.product.name }}</strong
              ><small
                >{{ money(item.product.cashPrice) }} por
                {{ item.product.unit }}</small
              >
            </div>
            <fy-field
              label="Cantidad"
              help="Unidades a financiar. Ejemplo: 2 o 1.500. Positivo, máximo 3 decimales."
              ><label class="sr-only" [for]="'qty-' + item.product.id"
                >Cantidad de {{ item.product.name }}</label
              ><input
                [id]="'qty-' + item.product.id"
                type="number"
                min="0.001"
                max="100000"
                step="0.001"
                [(ngModel)]="item.quantity"
                (ngModelChange)="invalidate()" /></fy-field
            ><button
              class="text-button"
              type="button"
              (click)="remove(item.product.id)"
              [attr.aria-label]="'Quitar ' + item.product.name"
            >
              Quitar
            </button>
          </div>
        } @empty {
          <div class="empty">
            <h4>La venta empieza aquí</h4>
            <p>Agrega uno o más productos del catálogo.</p>
          </div>
        }
        <div class="total-line">
          <span>Capital a financiar</span><strong>{{ money(total()) }}</strong>
        </div>
        <p class="fine">
          Se utiliza el precio al contado como capital para evitar duplicar
          intereses incluidos en el precio de lista.
        </p>
        <div class="form-grid">
          <fy-field
            label="Modalidad"
            help="Fin de mes genera un pago único; cuotas aplica método francés y gracia total. Debe estar permitida por cada producto."
            ><label class="sr-only" for="sale-mode">Modalidad</label
            ><select
              id="sale-mode"
              [(ngModel)]="mode"
              (ngModelChange)="
                invalidate(); months = mode === 'MONTH_END' ? 1 : 3
              "
            >
              <option value="INSTALLMENTS">Cuotas mensuales</option>
              <option value="MONTH_END">Fin de mes</option>
            </select></fy-field
          >
          <fy-field
            label="Número de cuotas"
            help="Plazo solicitado. Ejemplo: 3. Entero positivo hasta el máximo autorizado; pago único siempre utiliza 1."
            ><label class="sr-only" for="sale-months">Número de cuotas</label
            ><input
              id="sale-months"
              type="number"
              min="1"
              [max]="selected()?.terms.maxMonths ?? 120"
              step="1"
              [(ngModel)]="months"
              [disabled]="mode === 'MONTH_END'"
              (ngModelChange)="invalidate()"
          /></fy-field>
        </div>
        <button
          class="full secondary"
          type="button"
          [disabled]="busy || !accountId || !cart.length"
          (click)="simulate()"
        >
          {{ busy ? "Calculando…" : "Simular financiamiento" }}
        </button>
      </section>
    </div>
    @if (simulation) {
      <section class="panel schedule-panel">
        <div class="section-heading">
          <div>
            <h3>Revisa el cronograma antes de confirmar</h3>
            <p>
              Gracia: {{ simulation.graceDays }} días comerciales · Periodo
              cero: {{ simulation.periodZero | date: "dd/MM/yyyy" }} · TEM
              {{ simulation.tem * 100 | number: "1.7-7" }}%
            </p>
          </div>
          <strong>{{ money(simulation.total) }}</strong>
        </div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Cuota</th>
                <th>Vencimiento</th>
                <th class="numeric">Saldo inicial</th>
                <th class="numeric">Interés</th>
                <th class="numeric">Capital</th>
                <th class="numeric">Cuota</th>
                <th class="numeric">Saldo final</th>
              </tr>
            </thead>
            <tbody>
              @for (row of simulation.rows; track row.number) {
                <tr>
                  <td>{{ row.number }}</td>
                  <td>{{ row.dueDate | date: "dd/MM/yyyy" }}</td>
                  <td class="numeric">{{ money(row.opening) }}</td>
                  <td class="numeric">{{ money(row.interest) }}</td>
                  <td class="numeric">{{ money(row.principal) }}</td>
                  <td class="numeric">
                    <strong>{{ money(row.amount) }}</strong>
                  </td>
                  <td class="numeric">{{ money(row.closing) }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        <div class="form-actions">
          <span class="fine"
            >Al confirmar se conservarán los precios y las tasas pactadas.</span
          ><button type="button" [disabled]="busy" (click)="confirm()">
            {{ busy ? "Registrando…" : "Confirmar y registrar compra" }}
          </button>
        </div>
      </section>
    }`,
})
export class PointOfSaleComponent {
  private changeDetector = inject(ChangeDetectorRef);
  api = inject(ApiService);
  storeId = input.required<number>();
  customers = input.required<any[]>();
  products = input.required<any[]>();
  completed = output();
  accountId: number | null = null;
  query = "";
  cart: { product: any; quantity: number }[] = [];
  mode = "INSTALLMENTS";
  months = 3;
  simulation: any = null;
  error = "";
  success = "";
  busy = false;
  private operationKey = "";
  constructor() {
    effect(() => {
      this.storeId();
      this.accountId = null;
      this.cart = [];
      this.invalidate();
    });
  }
  selected() {
    return this.customers().find(
      (entry) => entry.account.id === this.accountId,
    );
  }
  filtered() {
    return this.products().filter(
      (product) =>
        product.active &&
        (product.name + " " + product.code)
          .toLowerCase()
          .includes(this.query.toLowerCase()),
    );
  }
  money(value: any, currency?: string) {
    return new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: currency ?? this.selected()?.account.currency ?? "PEN",
    }).format(Number(value));
  }
  total() {
    return this.cart.reduce(
      (sum, item) =>
        sum + Math.round(item.product.cashPrice * item.quantity * 100) / 100,
      0,
    );
  }
  add(product: any) {
    const item = this.cart.find((entry) => entry.product.id === product.id);
    if (item) item.quantity++;
    else this.cart.push({ product, quantity: 1 });
    this.invalidate();
  }
  remove(id: number) {
    this.cart = this.cart.filter((item) => item.product.id !== id);
    this.invalidate();
  }
  invalidate() {
    this.simulation = null;
    this.operationKey = "";
    this.error = "";
    this.success = "";
  }
  payload() {
    return {
      accountId: this.accountId,
      mode: this.mode,
      months: Number(this.months),
      items: this.cart.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
    };
  }
  async simulate() {
    try {
      this.busy = true;
      this.error = "";
      try {
        this.simulation = await this.api.post(
          `/stores/${this.storeId()}/sales/simulate`,
          this.payload(),
        );
        this.operationKey = crypto.randomUUID();
      } catch (error: any) {
        this.error =
          error.error?.message ?? "No fue posible simular la compra.";
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async confirm() {
    try {
      if (!this.simulation) return;
      this.busy = true;
      this.error = "";
      try {
        const purchase = await this.api.post(
          `/stores/${this.storeId()}/sales`,
          this.payload(),
          this.operationKey,
        );
        this.success = `Compra #${purchase.id} registrada. El cronograma ya está disponible en la cuenta corriente.`;
        this.cart = [];
        this.simulation = null;
        this.completed.emit();
      } catch (error: any) {
        this.error =
          error.error?.message ??
          "No se pudo confirmar. Reintenta para consultar la misma operación sin duplicarla.";
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
}
