# Paolicias.Sweet · Backend (microservicios)

Backend de la aplicación móvil **Paolicias.Sweet**, un servicio de reservas para pastelería, banquetería y catering. Está dividido en **4 microservicios Spring Boot** que se comunican por HTTP, guardan sus datos en **PostgreSQL (Supabase)** y se ejecutan como contenedores Docker en **Render**.

Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)** · Duoc UC · Experiencia 3 · **Grupo 11**.

| | |
|---|---|
| Docente | Eduardo Baeza |
| Integrantes | Milton Espinoza · Felipe Zapata · Jeremi Macaya |
| Repositorio de la app Android | https://github.com/jeremi457/Paolicias.sweet_Grupo11 |
| Repositorio del backend | `<URL-de-este-repositorio>` |

---

## 1. Arquitectura

![Mapa de microservicios](docs/diagrama-microservicios.png)

- La app Android (Retrofit) consume los cuatro servicios directamente, cada uno con su propia URL en Render.
- `reservas-service` consulta a `catalogo-service` para obtener el precio real y la disponibilidad del producto. **El precio lo decide el catálogo, nunca la app.**
- `pagos-service` consulta y actualiza el estado de la reserva en `reservas-service`.
- `auth-service` firma un **JWT** con `JWT_SECRET`. Los otros tres servicios lo verifican con el mismo secreto para exigir el rol `ADMIN` donde corresponde.
- El cambio de estado de una reserva solo se acepta entre servicios, mediante la cabecera `X-Internal-Key`.

### Flujo de una reserva con pago

![Flujo de reserva y pago](docs/flujo-reserva-pago.png)

---

## 2. Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje y framework | Java 17 · Spring Boot 4.1.2 (Spring MVC, Spring Data JPA) |
| Build | Maven (wrapper `mvnw` incluido en cada servicio) |
| Base de datos | PostgreSQL · Supabase en la nube, PostgreSQL 18 en Docker para desarrollo local |
| Seguridad | JWT con `jjwt` 0.13.0 · contraseñas con BCrypt (`spring-security-crypto`) |
| Comunicación entre servicios | `RestClient` de Spring con *timeouts* de conexión y lectura |
| Utilidades | Lombok |
| Contenedores | Docker (build multietapa: Maven → JRE 17) y Docker Compose |
| Nube | Render (servicios web) + Supabase (base de datos) |

---

## 3. Los cuatro microservicios

| Servicio | Puerto | Responsabilidad | Tablas |
|---|---|---|---|
| `auth-service` | 8081 | Login del administrador y emisión del JWT. Crea el administrador inicial al arrancar. | `administradores` |
| `catalogo-service` | 8082 | Productos (nombre, precio base, activo) y sus fotos. Carga 8 productos de ejemplo si la tabla está vacía. | `productos`, `imagenes_producto` |
| `reservas-service` | 8083 | Creación y consulta de reservas, cálculo de total y abono, máquina de estados. | `reservas` |
| `pagos-service` | 8084 | Comprobantes de pago y validación (aprobar o rechazar) por parte del administrador. | `pagos`, `comprobantes` |

Cada servicio sigue la misma estructura de paquetes: `controller` → `services` (interfaz + `Impl`) → `repository` → `entities`, además de `dto`, `security`, `config` y `client` cuando llama a otro servicio.

---

## 4. Endpoints

Leyenda de acceso: **Público** (sin token) · **ADMIN** (cabecera `Authorization: Bearer <jwt>` con rol `ADMIN`) · **Interno** (cabecera `X-Internal-Key`).

### auth-service (`:8081`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/login` | Público | Recibe `{ email, password }` y devuelve `{ token, nombre, rol }`. |
| GET | `/auth/salud` | Público | Verificación de que el servicio está arriba. |

### catalogo-service (`:8082`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/productos` | Público | Productos activos. |
| GET | `/productos/{id}` | Público | Detalle de un producto. |
| GET | `/productos/{id}/imagen` | Público | Foto subida del producto (bytes). |
| GET | `/productos/salud` | Público | Verificación del servicio. |
| GET | `/productos/todos` | ADMIN | Todos los productos, activos o no. |
| POST | `/productos` | ADMIN | Crea un producto (`201`). |
| PUT | `/productos/{id}` | ADMIN | Actualiza nombre, precio e imagen. |
| PATCH | `/productos/{id}/activo` | ADMIN | Activa o desactiva el producto. |
| POST | `/productos/{id}/imagen` | ADMIN | Sube la foto (`multipart`, campo `archivo`, JPG o PNG, máx. 5 MB). |

### reservas-service (`:8083`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/reservas` | Público | Crea una reserva en estado `PENDIENTE_PAGO` (`201`). |
| GET | `/reservas?email=` | Público | Reservas de un cliente, de la más nueva a la más antigua. |
| GET | `/reservas/{id}` | Público | Detalle de una reserva. |
| GET | `/reservas/todas` | ADMIN | Todas las reservas. |
| PATCH | `/reservas/{id}/estado` | Interno | Cambia el estado; solo lo usa `pagos-service`. |
| GET | `/reservas/salud` | Público | Verificación del servicio. |

### pagos-service (`:8084`)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/pagos/{reservaId}/comprobante` | Público | Sube el comprobante (`multipart`, campo `archivo`, JPG o PNG, máx. 5 MB). Deja el pago en `ENVIADO`. |
| GET | `/pagos/{reservaId}/comprobante` | ADMIN | Descarga la imagen del comprobante. |
| PATCH | `/pagos/{reservaId}/validacion` | ADMIN | Recibe `{ aprobado: true/false }` y confirma o rechaza la reserva. |
| GET | `/pagos/salud` | Público | Verificación del servicio. |

---

## 5. Reglas de negocio

**Reservas** (`reservas-service`)

- Correo del cliente obligatorio (con `@`) y producto existente y activo.
- Fecha en formato `aaaa-mm-dd`, no anterior a hoy (zona horaria `America/Santiago`).
- Horario: `Mañana`, `Tarde` o `Noche`.
- Asistentes: de 10 en 10, entre 10 y 100.
- Teléfono: exactamente 9 dígitos.
- Observaciones: máximo 200 caracteres.
- **Total** = `precioBase × (asistentes / 10)` · **Abono** = 50 % del total.

**Estados de una reserva**

```
PENDIENTE_PAGO ──► COMPROBANTE_ENVIADO ──► CONFIRMADA
                          │
                          └────────────────► RECHAZADA   (el cliente puede volver a subir comprobante)
```

**Pagos** (`pagos-service`)

- Solo imágenes JPG o PNG de hasta 5 MB.
- Una reserva ya confirmada no admite otro comprobante.
- Si se vuelve a subir un comprobante (por ejemplo tras un rechazo), reemplaza al anterior.
- Un pago solo se valida una vez: debe estar en `ENVIADO`.

**Catálogo** (`catalogo-service`)

- Nombre obligatorio, máximo 40 caracteres.
- Precio mayor a 0 y menor o igual a 9.999.999.

---

## 6. Modelo de datos

![Modelo de datos en Supabase](docs/modelo-datos.png)

| Tabla | Servicio dueño | Campos principales |
|---|---|---|
| `administradores` | auth | `id`, `email` (único), `nombre`, `password_hash` (BCrypt) |
| `productos` | catalogo | `id`, `nombre`, `precio_base`, `imagen`, `activo` |
| `imagenes_producto` | catalogo | `id`, `producto_id`, `content_type`, `datos` (bytea) |
| `reservas` | reservas | `id`, `cliente_email`, `producto_id`, `producto_nombre`, `fecha`, `horario`, `asistentes`, `telefono`, `observaciones`, `total`, `abono`, `estado`, `creada_en` |
| `pagos` | pagos | `id`, `reserva_id` (único), `monto`, `estado`, `enviado_en`, `validado_en` |
| `comprobantes` | pagos | `id`, `reserva_id`, `content_type`, `datos` (bytea) |

Cada microservicio es dueño de sus tablas y solo accede a ellas. Entre servicios no hay llaves foráneas: `reservas.producto_id` y `pagos.reserva_id` son referencias lógicas que se resuelven con llamadas HTTP. Hibernate crea y actualiza las tablas al arrancar (`ddl-auto: update`).

---

## 7. Variables de entorno

Los secretos nunca se suben al repositorio: `.env` está en `.gitignore` y se entrega una plantilla en `.env.example`.

| Variable | Usada por | Descripción |
|---|---|---|
| `DB_URL` | los 4 | URL JDBC de PostgreSQL. |
| `DB_USER` | los 4 | Usuario de la base de datos. |
| `DB_PASSWORD` | los 4 | Contraseña de la base de datos. |
| `JWT_SECRET` | los 4 | Secreto de firma del JWT. **Debe ser idéntico en todos los servicios.** |
| `JWT_EXP_MIN` | auth | Minutos de vigencia del token (por defecto 480). |
| `ADMIN_EMAIL` | auth | Correo del administrador inicial. |
| `ADMIN_PASSWORD` | auth | Contraseña del administrador inicial. |
| `ADMIN_NOMBRE` | auth | Nombre visible del administrador (por defecto `Administrador`). |
| `INTERNAL_KEY` | reservas, pagos | Clave compartida para las llamadas entre servicios. |
| `CATALOGO_URL` | reservas | URL base de `catalogo-service`. |
| `RESERVAS_URL` | pagos | URL base de `reservas-service`. |

Para el uso local con Docker Compose, el archivo `.env` usa además `POSTGRES_USER` y `POSTGRES_PASSWORD`.

---

## 8. Ejecución local

### Opción A · Docker Compose (recomendada)

Levanta PostgreSQL y los cuatro servicios.

```bash
git clone <URL-de-este-repositorio>
cd BackendPaoliciasGrupo11

cp .env.example .env
# Completa POSTGRES_USER, POSTGRES_PASSWORD, JWT_SECRET, ADMIN_EMAIL,
# ADMIN_PASSWORD, ADMIN_NOMBRE e INTERNAL_KEY

docker compose up --build
```

| Qué | Dónde |
|---|---|
| PostgreSQL | `localhost:5434` (bases `auth_db`, `catalogo_db`, `reservas_db`, `pagos_db`, creadas por `init-db/01-crear-bases.sql`) |
| auth-service | http://localhost:8081 |
| catalogo-service | http://localhost:8082 |
| reservas-service | http://localhost:8083 |
| pagos-service | http://localhost:8084 |

Para detener todo: `docker compose down` (agrega `-v` para borrar los datos).

### Opción B · Un servicio con Maven

Con una base accesible en `localhost:5434` y las variables de entorno definidas:

```bash
cd catalogo-service
./mvnw spring-boot:run
```

### Prueba rápida del flujo completo

```bash
# 1. Login del administrador
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"<ADMIN_EMAIL>","password":"<ADMIN_PASSWORD>"}'

# 2. Productos activos
curl http://localhost:8082/productos

# 3. Crear una reserva
curl -X POST http://localhost:8083/reservas \
  -H "Content-Type: application/json" \
  -d '{
        "clienteEmail": "cliente@correo.cl",
        "productoId": 1,
        "fecha": "2026-12-20",
        "horario": "Tarde",
        "asistentes": 30,
        "telefono": "912345678",
        "observaciones": "Sin frutos secos"
      }'

# 4. Subir el comprobante (ajusta el id de la reserva)
curl -X POST http://localhost:8084/pagos/1/comprobante \
  -F "archivo=@comprobante.jpg;type=image/jpeg"

# 5. Aprobar el pago como administrador
curl -X PATCH http://localhost:8084/pagos/1/validacion \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"aprobado": true}'
```

---

## 9. Despliegue en la nube: Supabase + Render

### 9.1 Base de datos en Supabase

1. Crea un proyecto en [supabase.com](https://supabase.com) y guarda la contraseña de la base de datos.
2. En el panel, botón **Connect**, elige **Session pooler** y copia los datos de conexión.
3. Arma las variables a partir de ese dato:

```
DB_URL=jdbc:postgresql://<POOLER-HOST>:5432/postgres?sslmode=require
DB_USER=postgres.<PROJECT-REF>
DB_PASSWORD=<contraseña de la base>
```

Puntos importantes:

- **Usa el Session pooler (puerto 5432), no la conexión directa.** La conexión directa de Supabase es IPv6 salvo que se contrate el complemento IPv4, y desde Render falla con *network is unreachable*. El pooler compartido funciona sobre IPv4.
- **Evita el Transaction pooler (puerto 6543)** con Hibernate, porque usa sentencias preparadas.
- El usuario del pooler lleva el sufijo del proyecto: `postgres.<PROJECT-REF>`, no solo `postgres`.
- Los cuatro servicios comparten la misma base (`postgres`) y no se pisan, porque cada uno crea solo sus propias tablas. Al primer arranque Hibernate crea el esquema, `auth-service` inserta el administrador y `catalogo-service` los 8 productos de ejemplo.
- Cada servicio abre su propio *pool* de conexiones. Para no agotar el límite del plan gratuito, se recomienda reducirlo en Render con `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3`.

### 9.2 Servicios en Render

Se crea **un Web Service por microservicio**, todos desde este mismo repositorio.

| Ajuste | Valor |
|---|---|
| Tipo | Web Service |
| Repositorio | este repositorio (rama `master`) |
| Runtime | Docker |
| Root Directory | `auth-service`, `catalogo-service`, `reservas-service` o `pagos-service` |
| Health Check Path | `/auth/salud` · `/productos/salud` · `/reservas/salud` · `/pagos/salud` |
| Variables de entorno | las de la sección 7 que corresponden a cada servicio |

Orden sugerido de despliegue, porque cada uno necesita la URL del anterior:

1. `auth-service` y `catalogo-service`.
2. `reservas-service`, con `CATALOGO_URL` apuntando al catálogo ya publicado.
3. `pagos-service`, con `RESERVAS_URL` apuntando a reservas.

Otras notas:

- Cada servicio escucha en su puerto fijo (8081 a 8084). Si Render no lo detecta solo, define la variable `PORT` con ese mismo valor.
- En el plan gratuito, Render suspende los servicios sin tráfico: la primera petición tras un rato de inactividad puede tardar bastante. Por eso la app Android usa *timeouts* de 120 segundos.
- Las URLs que la app usa hoy están definidas en `ApiConfig.kt` del proyecto Android. Si se recrea un servicio, hay que actualizarlas ahí.

---

## 10. Seguridad

- Contraseña del administrador guardada con **BCrypt**; el mensaje de error del login no distingue entre correo y contraseña incorrectos.
- El JWT incluye el rol y vence a los 480 minutos por defecto.
- Interceptores de Spring MVC validan el token y el rol `ADMIN` en las rutas de administración.
- La clave `X-Internal-Key` protege el cambio de estado de las reservas.
- Los secretos se entregan por variables de entorno y no se guardan en el código.

### Limitaciones conocidas y mejoras

- `GET /reservas?email=` y `GET /reservas/{id}` son públicos: cualquiera que conozca un correo o un id puede ver esa reserva. Una mejora natural es que los clientes también obtengan un JWT y filtrar por el correo del token.
- El registro y login de clientes viven hoy en la app (Room/SQLite). Mover los clientes a `auth-service` permitiría guardar contraseñas con hash en el servidor.
- Las fotos y comprobantes se guardan como `bytea` en PostgreSQL. Para mayor volumen convendría usar Supabase Storage.

---

## 11. Pruebas

Cada servicio incluye una clase `*ApplicationTests` con la prueba JUnit 5 `contextLoads` (`@SpringBootTest`), que comprueba que el contexto de Spring arranca. Necesita una base accesible y las variables de entorno definidas.

```bash
cd auth-service
./mvnw test
```

El `Dockerfile` compila con `-DskipTests` para que el despliegue no dependa de la base durante el build. Como siguiente paso, se pueden agregar pruebas unitarias de las reglas de negocio (`ReservaServiceImpl`, `PagoServiceImpl`, `ProductoServiceImpl`) con Mockito.

---

## 12. Estructura del repositorio

```
BackendPaoliciasGrupo11/
├── auth-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/cl/paolicias/auth/
│       ├── controller/   dto/   entities/   repository/
│       ├── security/     services/   config/
│       └── AuthServiceApplication.java
├── catalogo-service/     (misma estructura, paquete cl.paolicias.catalogo)
├── reservas-service/     (misma estructura, paquete cl.paolicias.reservas)
├── pagos-service/        (misma estructura, paquete cl.paolicias.pagos)
├── init-db/01-crear-bases.sql
├── docker-compose.yml
├── .env.example
└── docs/
```

---

## 13. Trabajo en equipo

- Ramas por integrante y *Pull Requests* hacia `master`.
- Planificación en Trello con épicas, historias de usuario y criterios de aceptación.
