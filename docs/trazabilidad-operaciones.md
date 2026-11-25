# Recorrido completo de las operaciones

## Compra

El usuario confirma en `PointOfSaleComponent.confirm`, en [point-of-sale.component.ts](../frontend/src/app/sales/point-of-sale.component.ts). El componente envía identificadores, cantidades, modalidad y plazo mediante [ApiService.post](../frontend/src/app/core/api.service.ts), con `Idempotency-Key`. El precio y las condiciones financieras definitivas se obtienen del servidor.

`POST /api/stores/{storeId}/sales` llega a [SalesController.purchase](../backend/src/main/java/pe/fiadoya/sales/api/SalesController.java). Bean Validation valida el DTO; Spring Security comprueba JWT, sesión y CSRF. [SalesService.purchase](../backend/src/main/java/pe/fiadoya/sales/application/SalesService.java) autoriza tienda y cuenta, bloquea/refresca la cuenta, comprueba la clave, valida productos y condiciones, aplica [FinancingPolicy](../backend/src/main/java/pe/fiadoya/sales/domain/FinancingPolicy.java) e invoca el motor puro.

[FinancialCalculationService.calculate](../backend/src/main/java/pe/fiadoya/finance/domain/FinancialCalculationService.java) coordina tasas, ciclo, gracia, interés y cronograma. La capa de aplicación registra `purchases`, `purchase_items`, `payment_obligations`, actualiza `credit_accounts.used` y agrega `audit_logs` mediante [Data](../backend/src/main/java/pe/fiadoya/shared/infrastructure/Data.java). Una transacción confirma todo o revierte todo. La respuesta contiene la compra con su ID; Angular actualiza las cuentas y comunica el registro.

```mermaid
sequenceDiagram
  actor Comercio
  participant UI as PointOfSaleComponent
  participant HTTP as ApiService
  participant API as SalesController
  participant App as SalesService
  participant Math as FinancialCalculationService
  participant Repo as Data/JPA
  participant DB as MySQL
  Comercio->>UI: Confirmar simulación
  UI->>HTTP: post(payload, Idempotency-Key)
  HTTP->>API: POST /stores/{storeId}/sales + cookie + CSRF
  API->>App: purchase(storeId, request, key)
  App->>Repo: Autorizar; bloquear y refrescar cuenta
  Repo->>DB: SELECT FOR UPDATE
  App->>App: Validar productos, moneda, plazo, tasa y línea
  App->>Math: calculate(principal, fecha, condiciones, modalidad, plazo)
  Math-->>App: capital, gracia y cronograma
  App->>Repo: Guardar compra, ítems, obligaciones, saldo y auditoría
  Repo->>DB: INSERT/UPDATE + COMMIT
  API-->>HTTP: Compra registrada con ID
  HTTP-->>UI: Mostrar confirmación y actualizar recursos
```

## Liquidación y pago

[AccountComponent.load](../frontend/src/app/accounts/account.component.ts) consulta `GET /api/stores/{storeId}/accounts/{id}/quote`. [PaymentController.quote](../backend/src/main/java/pe/fiadoya/payments/api/PaymentController.java) delega a [PaymentService.quote](../backend/src/main/java/pe/fiadoya/payments/application/PaymentService.java). Se consultan obligaciones no pagadas con vencimiento hasta la fecha operativa y se calcula mora usando la instantánea de la compra.

Después de confirmar la recepción del dinero, `AccountComponent.pay` envía `POST /api/stores/{storeId}/payments`. `PaymentService.pay` vuelve a calcular la liquidación bajo bloqueo de cuenta: la cotización visible no se acepta ciegamente como monto definitivo. [PaymentAllocationCalculator.allocate](../backend/src/main/java/pe/fiadoya/finance/domain/PaymentAllocationCalculator.java) exige importe exacto y descompone mora, compensatorio y capital.

Se insertan `payments` y `payment_allocations`, se marcan obligaciones pagadas, se disminuye la línea utilizada por el capital amortizado y se registra auditoría. La respuesta incluye pago, componentes y saldo posterior. La clave de operación se conserva durante reintentos de transporte y las restricciones únicas impiden cancelaciones duplicadas.

```mermaid
sequenceDiagram
  actor Comercio
  participant UI as AccountComponent
  participant HTTP as ApiService
  participant API as PaymentController
  participant App as PaymentService
  participant Math as LateInterest / PaymentAllocation
  participant DB as Data/JPA y MySQL
  UI->>HTTP: Consultar quote
  HTTP->>API: GET /accounts/{id}/quote
  API->>App: quote(storeId, accountId)
  App->>DB: Obligaciones exigibles y condiciones históricas
  App->>Math: Mora por obligación vencida
  API-->>UI: Desglose de liquidación
  Comercio->>UI: Confirmar recepción exacta
  UI->>HTTP: post(payment, Idempotency-Key)
  HTTP->>API: POST /payments + cookie + CSRF
  API->>App: pay(storeId, input, key)
  App->>DB: Bloquear/refrescar cuenta; revisar clave
  App->>App: Recalcular deuda exigible
  App->>Math: allocate(recibido,mora,interés,capital)
  Math-->>App: Imputación completa
  App->>DB: Pago, asignaciones, obligaciones pagadas y saldo + COMMIT
  API-->>UI: ID de pago y capital pendiente
```
