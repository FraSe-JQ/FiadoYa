# Guía de sustentación del código fuente

El proyecto se organiza en frontend (presentación), backend (API, aplicación, dominio y persistencia), infrastructure (referencias de despliegue), scripts (demostración y evidencia), tests (índice) y docs (informe). El módulo matemático puro no inicia Spring ni consulta MySQL.

## Tabla de trazabilidad del Capítulo 4

Apartado del informe | Fórmula | Archivo fuente | Clase | Método | Archivo de prueba
---|---|---|---|---|---
4.1 | i(d)=(1+i)^(d/D)-1; nominal (1+j/(D/c))^(d/c)-1 | [InterestRateConverter.java](../backend/src/main/java/pe/fiadoya/finance/domain/InterestRateConverter.java) | InterestRateConverter | effectiveRate | [InterestRateConverterTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/InterestRateConverterTest.java)
4.2 | 360 Δaño + 30 Δmes + Δdía normalizado | [CommercialDayCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/CommercialDayCalculator.java) | CommercialDayCalculator | between | [CommercialDayCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/CommercialDayCalculatorTest.java)
4.2 | compra <= corte; pago >= fecha del corte | [BillingCycleCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/BillingCycleCalculator.java) | BillingCycleCalculator | calculate | [BillingCycleCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/BillingCycleCalculatorTest.java)
4.3 | Pg=P+round(P*i(d),2) | [GracePeriodCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/GracePeriodCalculator.java) | GracePeriodCalculator | calculate | [GracePeriodCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/GracePeriodCalculatorTest.java)
4.4 y 4.7 | C=Pg*i*(1+i)^n/((1+i)^n-1) | [FrenchAmortizationCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/FrenchAmortizationCalculator.java) | FrenchAmortizationCalculator | installment; schedule | [FrenchAmortizationCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/FrenchAmortizationCalculatorTest.java)
4.5 | I=P*i(d) | [CompensatoryInterestCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/CompensatoryInterestCalculator.java) | CompensatoryInterestCalculator | calculate | [CompensatoryInterestCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/CompensatoryInterestCalculatorTest.java)
4.6 | M*((1+TEAm)^(max(0,d)/360)-1) | [LateInterestCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/LateInterestCalculator.java) | LateInterestCalculator | calculate | [LateInterestCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/LateInterestCalculatorTest.java)
4.8 | recibido=mora+interés+capital | [PaymentAllocationCalculator.java](../backend/src/main/java/pe/fiadoya/finance/domain/PaymentAllocationCalculator.java) | PaymentAllocationCalculator | allocate | [PaymentAllocationCalculatorTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/PaymentAllocationCalculatorTest.java)
4.8 | coordinación de 4.1–4.7 | [FinancialCalculationService.java](../backend/src/main/java/pe/fiadoya/finance/domain/FinancialCalculationService.java) | FinancialCalculationService | calculate | [FinancialCalculationServiceTest.java](../backend/src/test/java/pe/fiadoya/finance/domain/FinancialCalculationServiceTest.java)

## InterestRateConverter

El apartado 4.1 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/InterestRateConverter.java`, método `effectiveRate`. La fórmula es i(d)=(1+i)^(d/D)-1; nominal (1+j/(D/c))^(d/c)-1. Ejemplo explícito: 24% TNA mensual → TEM 0.02. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=InterestRateConverterTest test`; no requiere base de datos ni levantar la aplicación.

## CommercialDayCalculator

El apartado 4.2 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/CommercialDayCalculator.java`, método `between`. La fórmula es 360 Δaño + 30 Δmes + Δdía normalizado. Ejemplo explícito: 25/11/2026 a 06/12/2026 → 11 días. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=CommercialDayCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## BillingCycleCalculator

El apartado 4.2 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/BillingCycleCalculator.java`, método `calculate`. La fórmula es compra <= corte; pago >= fecha del corte. Ejemplo explícito: 06/10/2026 → corte 15/10, pago 25/10. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=BillingCycleCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## GracePeriodCalculator

El apartado 4.3 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/GracePeriodCalculator.java`, método `calculate`. La fórmula es Pg=P+round(P*i(d),2). Ejemplo explícito: 164.00, 19 días → 166.07. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=GracePeriodCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## FrenchAmortizationCalculator

El apartado 4.4 y 4.7 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/FrenchAmortizationCalculator.java`, método `installment; schedule`. La fórmula es C=Pg*i*(1+i)^n/((1+i)^n-1). Ejemplo explícito: 166.07, TEM .02, n=3 → 57.59,57.59,57.58. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=FrenchAmortizationCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## CompensatoryInterestCalculator

El apartado 4.5 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/CompensatoryInterestCalculator.java`, método `calculate`. La fórmula es I=P*i(d). Ejemplo explícito: 164.00, 19 días, TNA24% mensual → 2.07. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=CompensatoryInterestCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## LateInterestCalculator

El apartado 4.6 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/LateInterestCalculator.java`, método `calculate`. La fórmula es M*((1+TEAm)^(max(0,d)/360)-1). Ejemplo explícito: 57.59, TEAm35%, 11 días → 0.53. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=LateInterestCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## PaymentAllocationCalculator

El apartado 4.8 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/PaymentAllocationCalculator.java`, método `allocate`. La fórmula es recibido=mora+interés+capital. Ejemplo explícito: 58.12 → .53 mora,3.32 interés,54.27 capital. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=PaymentAllocationCalculatorTest test`; no requiere base de datos ni levantar la aplicación.

## FinancialCalculationService

El apartado 4.8 se localiza directamente en `backend/src/main/java/pe/fiadoya/finance/domain/FinancialCalculationService.java`, método `calculate`. La fórmula es coordinación de 4.1–4.7. Ejemplo explícito: 164.00 → total172.76 y saldo final0.00. La explicación de entradas, unidades, periodicidad, convención de días, precisión y casos especiales se encuentra en el JavaDoc del método.

La prueba individual se ejecuta con `mvn -f backend/pom.xml -Dtest=FinancialCalculationServiceTest test`; no requiere base de datos ni levantar la aplicación.

## Preguntas técnicas para la exposición

**¿Por qué BigDecimal?** Evita usar representación binaria aproximada como tipo principal de dinero y tasas. La precisión intermedia es 34 y los importes se redondean con HALF_UP a 2 decimales. big-math ejecuta la potencia fraccionaria, mientras la conversión y la fórmula permanecen visibles.

**¿Por qué la primera cuota no vence el 25/10?** Ese día termina la gracia total y constituye el periodo cero. El método francés vencido comienza un periodo mensual después, el 25/11.

**¿Por qué la última cuota es distinta?** Los intereses y cuotas se redondean por periodo. La última amortiza todo el saldo pendiente, por lo que garantiza cero sin dejar residuos.

**¿Por qué no se usa precio lista como principal?** FinancingPolicy centraliza el precio contado como base y conserva lista crédito como referencia independiente; se evita financiar intereses ya incluidos. El caso de control define 164 explícitamente.

**¿Qué cambia si se actualiza la tasa del cliente?** Se agrega una nueva versión en credit_terms. La compra conserva termsJson; la mora utiliza esa instantánea. WorkflowIntegrationTest cambia tasas y comprueba que la liquidación sigue siendo 58.12.

**¿Cómo se impide exceder el límite con dos compras simultáneas?** Se bloquea y refresca la cuenta dentro de una transacción; solo una operación puede validar y actualizar ese saldo a la vez. La prueba concurrente utiliza dos hilos y un límite de 200 frente a compras capitalizadas de 166.07.

**¿Dónde se evita pagar dos veces?** El servicio compara clave y hash de solicitud, bloquea la cuenta y cancela obligaciones completas. La base añade unicidad de clave por tienda y de obligación en payment_allocations.

**¿Qué limita el alcance legal?** La prohibición académica de pagos parciales no reemplaza el derecho de prepago. No se implementa el procedimiento legal de prepago ni se declara cumplimiento integral. Las tasas de prueba no son límites normativos.

**¿Qué es rendimiento contractual?** La tasa que iguala el desembolso con las cuotas fechadas bajo 30/360. No es utilidad, porque no se registran costos del negocio.

**¿Qué hizo la IA?** La participación se encuentra en registro-apoyo-ia.md. El grupo debe completar el puente humano y el porcentaje respecto del trabajo total sin inventar actividades.

Los recorridos completos de ventas y pagos se explican en [trazabilidad-operaciones.md](trazabilidad-operaciones.md). Los resultados ejecutados se reportan en [resultados-pruebas.md](resultados-pruebas.md).

**¿Ser administrador en una tienda permite leer clientes de otra?** No. `Access.managesStore` exige una membresía en el comercio consultado. Si un administrador también figura como cliente de otra tienda, `Access.account` y `CustomerController.customers` permiten únicamente su cuenta. La prueba `storeAdministratorWhoIsCustomerElsewhereCannotReadOtherCustomersThere` verifica el caso; una E2E comprueba que el menú cambia al modo cliente al seleccionar ese comercio.
