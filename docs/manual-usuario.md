# Manual de usuario

1. El usuario se registra con nombre, correo y contraseña de 10–72 caracteres. El registro público crea un cliente; no concede permisos administrativos.
2. La plataforma registra el establecimiento y asigna un administrador existente desde Configuración. No se requiere volver a registrar la cuenta del administrador.
3. El administrador selecciona el comercio, registra productos y mantiene precio contado, lista crédito, moneda y modalidad. La baja lógica se realiza desmarcando Activo.
4. En Clientes se registran identificación y condiciones de crédito. Cada signo ? explica finalidad, formato, ejemplo y restricciones del campo. DNI/RUC se validan por formato, sin consultar registros oficiales.
5. La vinculación con un usuario requiere ID registrado, correo coincidente y consentimiento. El mismo usuario puede consultar cuentas en varios comercios.
6. En Nueva venta se selecciona el cliente, se agregan productos, se indican cantidades y se simula. La pantalla muestra crédito disponible, tasa, gracia y cronograma. La confirmación registra la compra de forma atómica.
7. Cuentas y pagos presenta capital, intereses, deuda vencida, obligaciones, compras, pagos y cierres. El CSV descarga el detalle del cronograma.
8. La liquidación muestra todas las obligaciones exigibles en la fecha operativa. El administrador verifica la recepción exacta, selecciona el medio de pago y confirma. El sistema no realiza un cobro bancario ni permite liquidaciones parciales.
9. Reportes muestra cartera, proyección de cobros, rendimiento contractual y auditoría. Las monedas se consultan separadamente.
10. Ayuda y soporte permite consultar instrucciones y registrar solicitudes. No existe envío automático a correo o WhatsApp. La plataforma puede editar los artículos.
11. Salir revoca las sesiones activas del usuario. La sesión también expira después de una hora.

En demostración, la plataforma ajusta la fecha desde Configuración; el banner identifica el entorno y la fecha simulada. El reloj no modifica las fechas reales del equipo y se reinicia al reiniciar la API.
