import { ChangeDetectorRef } from "@angular/core";
import { YieldComponent } from "./reporting/yield.component";
import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "./core/api.service";
import { AuthComponent } from "./iam/auth.component";
import { EditorComponent, EditorField } from "./shared/editor.component";
import { FieldComponent } from "./shared/field.component";
import { PointOfSaleComponent } from "./sales/point-of-sale.component";
import { AccountComponent } from "./accounts/account.component";
import {
  customerFields,
  productFields,
  storeFields,
  defaultCustomer,
  customerPayload,
  flattenCustomer,
} from "./core/form-definitions";
@Component({
  selector: "app-root",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    AuthComponent,
    EditorComponent,
    FieldComponent,
    PointOfSaleComponent,
    AccountComponent,
    YieldComponent,
  ],
  templateUrl: "./app.component.html",
})
export class AppComponent implements OnInit {
  private changeDetector = inject(ChangeDetectorRef);
  api = inject(ApiService);
  user: any = null;
  initialized = false;
  stores: any[] = [];
  customers: any[] = [];
  products: any[] = [];
  dashboard: any = null;
  articles: any[] = [];
  tickets: any[] = [];
  users: any[] = [];
  audit: any[] = [];
  limits: any[] = [];
  storeId: number | null = null;
  page = "dashboard";
  busy = false;
  error = "";
  message = "";
  query = "";
  editorType = "";
  editingId: number | null = null;
  fields: EditorField[] = [];
  initial: Record<string, any> = {};
  clock: any = null;
  clockValue = "";
  currency = "PEN";
  mobileNav = false;
  assignUserId: number | null = null;
  assignStoreId: number | null = null;
  supportSubject = "";
  supportMessage = "";
  nav = [
    { id: "dashboard", label: "Resumen", icon: "◫" },
    { id: "stores", label: "Establecimientos", icon: "▦" },
    { id: "customers", label: "Clientes", icon: "♙" },
    { id: "products", label: "Catálogo", icon: "▤" },
    { id: "sales", label: "Nueva venta", icon: "+" },
    { id: "accounts", label: "Cuentas y pagos", icon: "≡" },
    { id: "reports", label: "Reportes", icon: "▥" },
    { id: "help", label: "Ayuda y soporte", icon: "?" },
    { id: "settings", label: "Configuración", icon: "⚙" },
  ];
  async ngOnInit() {
    try {
      try {
        await this.api.get("/auth/csrf");
        this.user = await this.api.get("/auth/me");
        await this.load();
      } catch {
      } finally {
        this.initialized = true;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  isCustomer() {
    return (
      this.user?.role === "CUSTOMER" ||
      (this.user?.role === "STORE_ADMIN" &&
        this.storeId !== null &&
        !this.user.managedStoreIds?.includes(this.storeId))
    );
  }
  isPlatform() {
    return this.user?.role === "PLATFORM_ADMIN";
  }
  visibleNav() {
    return this.nav.filter(
      (item) => !this.isCustomer() || ["accounts", "help"].includes(item.id),
    );
  }
  store() {
    return this.stores.find((store) => store.id === this.storeId);
  }
  stats() {
    return this.dashboard?.currencies?.[this.currency];
  }
  title() {
    return this.nav.find((item) => item.id === this.page)?.label ?? "FiadoYa";
  }
  money(value: any) {
    return new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: this.currency,
    }).format(Number(value ?? 0));
  }
  async authenticated(user: any) {
    try {
      this.user = user;
      this.page = this.isCustomer() ? "accounts" : "dashboard";
      await this.load();
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async load() {
    try {
      this.busy = true;
      try {
        this.stores = await this.api.get("/stores");
        this.clock = await this.api.get("/clock");
        this.clockValue = this.clock.dateTime.slice(0, 16);
        if (!this.storeId || !this.stores.some((s) => s.id === this.storeId))
          this.storeId = this.stores[0]?.id ?? null;
        if (this.isCustomer()) this.page = "accounts";
        await this.loadStore();
      } catch (error: any) {
        this.showError(error);
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async loadStore() {
    try {
      if (!this.storeId) {
        this.customers = [];
        this.products = [];
        this.dashboard = null;
        return;
      }
      this.busy = true;
      try {
        const base = `/stores/${this.storeId}`;
        [this.customers, this.products] = await Promise.all([
          this.api.get(base + "/customers"),
          this.api.get(base + "/products"),
        ]);
        if (!this.isCustomer())
          this.dashboard = await this.api.get(base + "/dashboard");
        else {
          this.dashboard = null;
          this.page = "accounts";
        }
      } catch (error: any) {
        this.showError(error);
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async navigate(page: string) {
    try {
      this.page = page;
      this.editorType = "";
      this.error = "";
      this.message = "";
      this.query = "";
      this.mobileNav = false;
      try {
        this.clock = await this.api.get("/clock");
        if (["dashboard", "reports"].includes(page)) await this.loadStore();
        if (page === "help") {
          [this.articles, this.tickets] = await Promise.all([
            this.api.get("/help"),
            this.api.get("/support"),
          ]);
        }
        if (page === "settings") {
          this.limits = await this.api.get("/rate-limits");
          if (this.isPlatform()) this.users = await this.api.get("/users");
        }
        if (page === "reports" && this.storeId)
          this.audit = await this.api.get(`/stores/${this.storeId}/audit`);
      } catch (error: any) {
        this.showError(error);
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  filteredCustomers() {
    return this.customers.filter((entry) =>
      (
        entry.customer.firstName +
        " " +
        entry.customer.lastName +
        " " +
        entry.customer.documentNumber
      )
        .toLowerCase()
        .includes(this.query.toLowerCase()),
    );
  }
  filteredProducts() {
    return this.products.filter((product) =>
      (product.name + " " + product.code)
        .toLowerCase()
        .includes(this.query.toLowerCase()),
    );
  }
  openEditor(type: string, entry?: any) {
    this.editorType = type;
    this.error = "";
    this.message = "";
    this.editingId = entry?.id ?? entry?.customer?.id ?? null;
    if (type === "store") {
      this.fields = storeFields;
      this.initial = entry ?? {
        name: "",
        businessType: "",
        address: "",
        phone: "",
        active: true,
      };
    }
    if (type === "product") {
      this.fields = productFields;
      this.initial = entry ?? {
        code: "",
        name: "",
        description: "",
        supplier: "",
        brand: "",
        unit: "unidad",
        imageUrl: "",
        cashPrice: null,
        creditPrice: null,
        currency: "PEN",
        mode: "BOTH",
        active: true,
      };
    }
    if (type === "customer") {
      this.fields = customerFields;
      this.initial = entry ? flattenCustomer(entry) : { ...defaultCustomer };
    }
    if (type === "article") {
      this.fields = [
        {
          key: "title",
          label: "Título",
          help: "Título de ayuda. Ejemplo: Registrar una compra. Obligatorio, hasta 150 caracteres.",
        },
        {
          key: "content",
          label: "Contenido",
          type: "textarea",
          help: "Instrucciones visibles a usuarios. Texto claro, ejemplo y restricciones; hasta 4000 caracteres.",
        },
      ];
      this.initial = entry;
    }
    if (type === "limit") {
      this.editingId = null;
      this.fields = [
        {
          key: "currency",
          label: "Moneda",
          options: [
            { value: "PEN", label: "PEN" },
            { value: "USD", label: "USD" },
          ],
          help: "Moneda afectada por el límite. Ejemplo: PEN. Seleccione soles o dólares.",
        },
        {
          key: "kind",
          label: "Concepto",
          options: [
            { value: "COMPENSATORY", label: "Compensatorio" },
            { value: "PENALTY", label: "Moratorio" },
          ],
          help: "Tipo de tasa limitada. Ejemplo: compensatoria. Se verifica al registrar nuevas compras.",
        },
        {
          key: "maxTeaPercent",
          label: "TEA máxima (%)",
          type: "number",
          min: 0,
          help: "Límite verificado como porcentaje anual efectivo. Ejemplo de formato: 10.0000000. No ingrese valores sin fuente.",
        },
        {
          key: "validFrom",
          label: "Vigente desde",
          type: "date",
          help: "Inicio de vigencia inclusivo. Formato fecha, ejemplo: 2026-01-01.",
        },
        {
          key: "validUntil",
          label: "Vigente hasta",
          type: "date",
          help: "Fin de vigencia inclusivo. Ejemplo: 2026-06-30. No puede preceder al inicio.",
        },
        {
          key: "source",
          label: "Fuente verificable",
          help: "Referencia oficial de la tasa y vigencia. Ejemplo: URL de publicación BCRP. Obligatoria, hasta 255 caracteres.",
        },
      ];
      this.initial = { currency: "PEN", kind: "COMPENSATORY" };
    }
  }
  async save(values: Record<string, any>) {
    try {
      this.busy = true;
      try {
        let path =
          this.editorType === "store"
            ? "/stores"
            : this.editorType === "article"
              ? "/help"
              : this.editorType === "limit"
                ? "/rate-limits"
                : `/stores/${this.storeId}/${this.editorType === "customer" ? "customers" : "products"}`;
        const payload =
          this.editorType === "customer" ? customerPayload(values) : values;
        if (this.editingId)
          await this.api.put(path + "/" + this.editingId, payload);
        else await this.api.post(path, payload);
        this.editorType = "";
        this.message = "Cambios guardados correctamente.";
        await this.load();
        if (this.page === "help" || this.page === "settings")
          await this.navigate(this.page);
      } catch (error: any) {
        this.showError(error);
      } finally {
        this.busy = false;
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async assign() {
    try {
      if (!this.assignUserId || !this.assignStoreId) return;
      try {
        await this.api.post(
          `/stores/${this.assignStoreId}/admins/${this.assignUserId}`,
          {},
        );
        this.message = "Administrador asignado al establecimiento.";
      } catch (error: any) {
        this.showError(error);
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async setClock() {
    try {
      try {
        this.clock = await this.api.post("/clock", {
          dateTime: this.clockValue + ":00",
        });
        await this.loadStore();
        this.message = "Fecha de demostración actualizada.";
      } catch (error: any) {
        this.showError(error);
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async closeCycles() {
    try {
      try {
        const result = await this.api.post(
          `/stores/${this.storeId}/cycles/close`,
          {},
        );
        this.message = `Cierres generados: ${result.generated}. Los cierres existentes se conservan sin duplicarse.`;
      } catch (error: any) {
        this.showError(error);
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async support() {
    try {
      try {
        await this.api.post("/support", {
          subject: this.supportSubject,
          message: this.supportMessage,
        });
        this.supportSubject = "";
        this.supportMessage = "";
        this.tickets = await this.api.get("/support");
        this.message = "Solicitud guardada para revisión del equipo.";
      } catch (error: any) {
        this.showError(error);
      }
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  async logout() {
    try {
      await this.api.post("/auth/logout", {});
      this.user = null;
      this.storeId = null;
      this.customers = [];
      this.products = [];
    } finally {
      this.changeDetector.markForCheck();
    }
  }
  showError(error: any) {
    this.error =
      error.error?.message ??
      "No se pudo completar la operación. Revisa la conexión e inténtalo de nuevo.";
  }
}
