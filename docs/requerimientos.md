# Requerimientos y fuentes

FiadoYa corresponde al curso SI642, UPC, ciclo 2026-20, sección 7658, Grupo 4. El objetivo es sustituir el registro manual del crédito comercial. El comercio es el usuario operativo principal.

Se analizaron los seis archivos originales disponibles en `reference/`: enunciado de ocho páginas, retroalimentación del Grupo 4, instrucciones de exposición, informe parcial y las rúbricas ABET e ICACIT. Los documentos privados y sus extracciones no se incorporan a Git. El directorio recibido se denomina reference, no references.

La prioridad adoptada es enunciado, retroalimentación, instrucciones del segundo hito, rúbricas e informe parcial. Los requisitos verificables son:

| Identificador | Resultado requerido | Implementación |
|---|---|---|
| IAM-01 | Registro público de cliente; autenticación y cierre de sesión | iam |
| TEN-01 | Autorización por rol, tienda y titularidad | Access |
| STO-01 | Alta, edición, baja lógica y administradores | StoreController |
| CUS-01 | Clientes y condiciones versionadas | CustomerController, CreditService |
| CAT-01 | Catálogo, dos precios y modalidad | ProductController |
| SAL-01 | Venta multiproducto, simulación, bloqueo y transacción | SalesService |
| FIN-01 | Tasas nominales/efectivas compensatorias y moratorias | finance/domain |
| FIN-02 | Gracia, francés, pago único y mora | calculadores identificables |
| PAY-01 | Liquidación completa, prelación y no duplicación | PaymentService |
| ACC-01 | Capital, intereses, obligaciones, compras y pagos | AccountController |
| CYC-01 | Cierre programado idempotente e histórico | CycleService, CycleScheduler |
| REP-01 | Cartera, proyección, rendimiento y CSV | reporting, ReportController |
| HLP-01 | Ayuda por todos los campos y soporte persistido | FieldComponent, EditorComponent |
| OPS-01 | Docker, migraciones, salud y volumen persistente | docker-compose.yml |

No se incluyen stock, impuestos, facturación electrónica ni integración RENIEC/SUNAT. La validación documental es exclusivamente de formato. Los procedimientos legales de prepago requieren una ampliación explícita y validación especializada.
