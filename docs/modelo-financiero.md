# Modelo financiero

## Convenciones y glosario

El precio contado es el valor del producto sin financiamiento. El precio de lista al crédito es un dato comercial independiente; no constituye automáticamente el principal de una nueva operación con intereses. La política FinancingPolicy utiliza el precio contado como capital P y conserva ambos precios en el ítem histórico. El capital de las pruebas se determina explícitamente mediante productos cuyos precios suman S/ 164,00.

La línea de crédito es el máximo capital pendiente autorizado. Crédito utilizado = capital pendiente original + gracia capitalizada pendiente. Crédito disponible = límite - utilizado. No consumen línea los intereses compensatorios futuros ni la mora. La amortización libera únicamente capital. El estado de cuenta reúne compras, obligaciones y pagos sin confundir capital con total exigible.

Los importes se representan con BigDecimal. MathContext de 34 cifras significativas y HALF_UP; importes monetarios a 2 decimales. Las tasas porcentuales se dividen entre 100. Las tasas equivalentes se conservan con más de 7 decimales; la presentación no alimenta el cálculo.

La convención es **30E/360 con todos los finales de mes normalizados a 30, incluido febrero**. Cada fecha aporta día 30 si es fin de mes; en otro caso se usa min(día,30). La diferencia es 360*(año2-año1)+30*(mes2-mes1)+día2-día1. Por ejemplo, 28/02/2026 a 31/03/2026 = 30 días comerciales. Es una convención académica explícita; no equivale a días naturales ni se afirma que sea idéntica a toda variante europea/ISDA. La hora opera en America/Lima y determina el ciclo, no fracciones de día financiero.

## 4.1 Conversión de tasas

Efectiva de D días: i(d)=(1+i(D))^(d/D)-1. Nominal j de D días con capitalización cada c días: m=D/c; i(d)=(1+j/m)^(d/c)-1. D y c deben ser compatibles, enteros y positivos; c divide a D. TEA=i(360), TEM=i(30), TED=i(1). El mismo algoritmo convierte tasas compensatorias y moratorias. La biblioteca big-math ejecuta la potenciación decimal; las fórmulas permanecen visibles en InterestRateConverter.

## 4.2 Ciclo y calendario

Compra <= fecha-hora de corte pertenece al ciclo actual. Una compra posterior pertenece al siguiente mes. Se limita el día de corte al último del mes. Se obtiene el día de pago del mes del corte y, si precede al corte, del siguiente mes. Las cuotas siguientes usan YearMonth y el día mensual pactado; no se suman literalmente 30 días calendario. El límite de corte 23:59 corresponde a 23:59:00 cuando no se especifican segundos.

## 4.3 Gracia total

d=days30_360(compra,pagoCiclo); Igracia=round(P*i(d),2); Pg=P+Igracia. Para cuotas, el periodo cero es el final de gracia y la primera cuota vence un mes después. Para pago único no se capitaliza gracia en la línea: el interés se muestra como compensatorio de la obligación.

## 4.4 Método francés

C=Pg*[TEM*(1+TEM)^n]/[(1+TEM)^n-1]. Para TEM=0: C=Pg/n. Cada periodo utiliza interés=round(saldoInicial*TEM,2), amortización=C-interés y saldoFinal=saldoInicial-amortización. La última amortización equivale a todo el saldo inicial y la cuota final se ajusta. Se impide amortización negativa.

## 4.5 Pago único

Icomp=round(P*((1+TEA)^(d/360)-1),2); monto=P+Icomp. Se registra una obligación con fecha de pago del ciclo y se conserva la tasa pactada.

## 4.6 Mora

dMora=max(0,days30_360(vencimiento,pagoReal)); Imora=round(M*((1+TEAm)^(dMora/360)-1),2). M es el importe de la obligación vencida, incluyendo su compensatorio, conforme al enunciado académico. No se aplican intereses moratorios a cuotas futuras ni a obligaciones pagadas. La tasa proviene de termsJson de la compra, nunca de la versión actual del cliente.

## 4.7 Cronograma y cierre

Cada obligación conserva número, fecha, saldo inicial, interés, capital, cuota, saldo final y estado. El cierre persiste una instantánea JSON por cuenta y corte; una restricción única evita duplicaciones. El proceso programado corre cada minuto y recupera cortes pendientes de compras. La acción manual permite la demostración con reloj simulado.

## 4.8 Pago y rendimiento

La liquidación ordinaria comprende **todas** las obligaciones pendientes con vencimiento <= fecha operativa. Se exige importe exacto, sin pagos parciales ni excesos. PaymentAllocationCalculator aplica mora, compensatorio y capital en ese orden; se registra cada concepto y cada obligación cancelada. El prepago de capital es un procedimiento distinto que no está implementado.

El rendimiento contractual se obtiene resolviendo -P+Σ(Ck/(1+r)^(dk/360))=0 mediante bisección decimal con expansión del intervalo para no limitar artificialmente tasas elevadas. Los flujos se sitúan en las fechas contratadas y el desembolso en la compra. No se incorporan costos, ingresos de venta al contado ni mora; no se deduce rentabilidad global. El cálculo requiere flujos posteriores al desembolso.

## Casos de control

TNA 24% mensual: TEM 2%; P=164,00; gracia=19 días; Pg=166,07. Cuotas 57,59; 57,59; 57,58; total 172,76. Vencimientos 25/11/2026, 25/12/2026 y 25/01/2027. Periodo cero 25/10/2026. Pago 06/12/2026: 11 días comerciales, mora a TEA 35% = 0,53; liquidación 58,12; capital futuro 111,80. La tasa moratoria es exclusivamente un dato de prueba.
