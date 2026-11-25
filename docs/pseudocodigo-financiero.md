# Capítulo 4 — pseudocódigo corregido

## 4.1 Conversión y normalización de tasas

```text
CONVERTIR(tasaPorcentual, tipo, D, c, d):
  validar porcentaje >= 0, D entre 1 y 360, d >= 0
  tasaDecimal = tasaPorcentual / 100
  SI tipo = EFECTIVA: retornar (1+tasaDecimal)^(d/D)-1
  validar c > 0 Y D mod c = 0
  m = D/c
  retornar (1+tasaDecimal/m)^(d/c)-1
```

## 4.2 Evaluación de compra, corte y pago

```text
mes = mesCalendario(fechaHoraCompra)
corte = fechaLimitadaAlFinDeMes(mes,diaCorte) + horaCorte
SI compra > corte: mes = mes+1; recalcular corte
pago = fechaLimitadaAlFinDeMes(mes,diaPago)
SI pago < fecha(corte): pago = fechaLimitadaAlFinDeMes(mes+1,diaPago)
d = diferenciaComercial(compra.fecha,pago)
```

## 4.3 Periodo de gracia y capitalización

```text
interesGracia = redondear(P*CONVERTIR(tasaCompensatoria,d),2,HALF_UP)
Pg = P+interesGracia
periodoCero = pagoDelCiclo
```

## 4.4 Cuotas por método francés

```text
TEM = CONVERTIR(tasaCompensatoria,30)
SI TEM=0: C=redondear(Pg/n,2)
SINO: C=redondear(Pg*TEM*(1+TEM)^n/((1+TEM)^n-1),2)
saldo=Pg
PARA k=1..n:
  fechaVencimiento=diaPactadoDelMes(periodoCero.mes+k)
  interes=redondear(saldo*TEM,2)
  amortizacion=SI k=n ENTONCES saldo SINO min(C-interes,saldo)
  validar amortizacion>=0
  cuota=amortizacion+interes
  saldoFinal=saldo-amortizacion
  agregarFila(k,fechaVencimiento,saldo,interes,amortizacion,cuota,saldoFinal)
  saldo=saldoFinal
```

## 4.5 Interés compensatorio de pago único

```text
I=redondear(P*CONVERTIR(tasaCompensatoria,diasCompraPago),2)
obligacion=(pagoDelCiclo,capital=P,interes=I,importe=P+I)
```

## 4.6 Interés moratorio

```text
diasMora=max(0,diferenciaComercial(vencimiento,fechaRealPago))
tasaMora=condicionesHistoricasDeCompra.penalty
mora=redondear(obligacionPendiente.importe*CONVERTIR(tasaMora,diasMora),2)
```

## 4.7 Listado y cronograma

Las filas se generan en orden de cuota. Las compras se listan por fecha e identificador. Cada cierre guarda las compras del corte y sus obligaciones como instantánea; una clave única cuenta/corte evita repetición.

## 4.8 Proceso consolidado

```text
REGISTRAR_COMPRA(usuario,tienda,cuenta,items,modalidad,n,clave):
  iniciar transaccion
  autorizar usuario,tienda,cuenta
  bloquear Y refrescar cuenta
  SI existe clave: verificar hash; retornar compra existente
  leer condiciones vigentes
  validar cliente activo, productos de tienda, monedas, modalidad y cantidades
  P=sum(redondear(precioContadoHistorico*cantidad,2))
  calcular ciclo, gracia, tasas equivalentes y obligaciones
  validar plazo, limite vigente de tasas y usado+capitalCapitalizado<=limite
  guardar compra con condiciones historicas y sus items
  guardar obligaciones; incrementar usado; guardar auditoria
  confirmar transaccion; retornar compra

REGISTRAR_PAGO(usuario,tienda,cuenta,recibido,medio,clave):
  iniciar transaccion; autorizar; bloquear Y refrescar cuenta
  SI existe clave: verificar hash; retornar pago existente
  fechaRealPago=relojOperativoLima
  obtener todas las obligaciones pendientes con vencimiento<=fechaRealPago
  calcular mora con tasa historica por obligacion
  exigir recibido=sum(mora+compensatorio+capital), mayor a cero
  imputar mora, despues compensatorio, despues capital
  guardar pago y asignaciones; marcar obligaciones pagadas
  disminuir usado por capital amortizado; impedir saldo negativo
  guardar auditoria; confirmar transaccion; retornar pago
```
