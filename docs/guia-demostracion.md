# Guía de demostración reproducible

## Preparación

Desde la raíz: `node scripts/setup-env.cjs`, `docker compose up --build -d`, `node scripts/demo-seed.cjs`. La aplicación queda en http://localhost:4200. Las credenciales de plataforma están en .env y las de usuarios ficticios en .local/demo-credentials.json. Nunca deben publicarse. El script no imprime contraseñas.

Se generan dos tiendas de distinto giro, administradores separados, cuatro cuentas, cinco productos, cuatro compras y un pago. Un cliente tiene cuenta en ambos establecimientos. La ejecución conserva un marcador local para no repetir la carga completa.

## Recorrido

Se presenta el problema y el modelo financiero; después se muestra el sistema. Se registra un nuevo usuario, se inicia sesión y la plataforma le asigna un comercio. El administrador registra dos productos a 52 y 60 soles, crea un cliente con TNA 24% mensual, corte 15 a 23:59 y pago 25, plazo 3 y mora efectiva anual 35% de prueba. Con reloj 06/10/2026 10:38 se registran 2 unidades del primer producto y 1 del segundo.

La simulación debe mostrar capital 164,00; gracia 19 días; capital capitalizado 166,07; cuotas 57,59, 57,59 y 57,58. La plataforma cambia el reloj a 06/12/2026 10:00; la liquidación de la primera cuota debe ser 58,12 y el saldo posterior 111,80. Las cuotas futuras no generan mora.

Se consulta el cronograma, el CSV y los registros SQL con scripts/sql-evidence.sql. Se cambia la tasa del cliente para demostrar que la compra conserva su copia histórica. Se ingresa como administrador de la otra tienda y como cliente para demostrar aislamiento. Se abre Swagger y se ejecuta una prueba individual del algoritmo francés.

## Presentación semana 15

La guía recibida dispone 20 minutos y 5 de preguntas: aproximadamente 4 de resumen del primer hito, 4 de desarrollo técnico y 12 de demostración. Deben cubrirse conectividad, base de datos, código, ayuda y pruebas. La exposición y las respuestas individuales corresponden a los integrantes; no se inventa evidencia de participación humana.

La preparación del video, presentación y documentos finales editables de entrega académica corresponde a una actividad posterior del equipo; no se afirma que estos materiales hayan sido producidos en esta implementación.
