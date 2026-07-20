# Dossier del Proyecto — Formulario de Comisiones

**Repositorio:** [Ettorinho/FormularioDeComisiones](https://github.com/Ettorinho/FormularioDeComisiones)
**Organismo destinatario:** Gobierno de Aragón
**Tipo de proyecto:** Aplicación web Java EE (WAR)
**Versión:** 1.0-SNAPSHOT
**Fecha del dossier:** 13/05/2026

---

## Tabla de contenidos

1. [Resumen ejecutivo](#1-resumen-ejecutivo)
2. [Objetivos y alcance funcional](#2-objetivos-y-alcance-funcional)
3. [Stack tecnológico](#3-stack-tecnológico)
4. [Arquitectura de la aplicación](#4-arquitectura-de-la-aplicación)
5. [Estructura del repositorio](#5-estructura-del-repositorio)
6. [Modelo de datos](#6-modelo-de-datos)
7. [Módulos funcionales](#7-módulos-funcionales)
8. [Seguridad y control de accesos](#8-seguridad-y-control-de-accesos)
9. [Generación dinámica de documentos (PDF y Word)](#9-generación-dinámica-de-documentos-pdf-y-word)
10. [Auditoría y trazabilidad](#10-auditoría-y-trazabilidad)
11. [Configuración y despliegue](#11-configuración-y-despliegue)
12. [Migraciones de base de datos](#12-migraciones-de-base-de-datos)
13. [Calidad, pruebas y observabilidad](#13-calidad-pruebas-y-observabilidad)
14. [Mejoras futuras y roadmap](#14-mejoras-futuras-y-roadmap)
15. [Anexos](#15-anexos)

---

## 1. Resumen ejecutivo

**Formulario de Comisiones** es una aplicación web corporativa desarrollada para el **Gobierno de Aragón** cuyo objetivo es **gestionar de forma integral las comisiones, grupos de trabajo y grupos de mejora**, sus miembros, los cargos que desempeñan, las reuniones celebradas (actas) y la asistencia a las mismas.

La aplicación está construida en **Java 11** sobre el modelo clásico de **Servlets + JSP + JSTL** desplegable como WAR en **Apache Tomcat 9**, con persistencia en **PostgreSQL** mediante un *connection pool* **HikariCP** gestionado vía **JNDI**. La autenticación se delega en **Active Directory** mediante LDAP, y la autorización se basa en un modelo de tres roles (**ADMIN, GESTOR, LECTURA**) mapeados a grupos AD.

Entre sus características destacan:

- Gestión completa del ciclo de vida de comisiones, miembros y cargos, con **historial de cambios de cargo**.
- Registro de **actas de reunión** con control de asistencias, justificaciones y posibilidad de **adjuntar PDF**.
- **Generación dinámica** de actas en **PDF (Apache PDFBox)** y **Word .docx (Apache POI)**.
- **Auditoría centralizada** de acciones (creación, modificación, baja, login, logout, accesos denegados, etc.).
- Identidad visual corporativa basada en **Bootstrap 5.3** y los colores institucionales del Gobierno de Aragón.

---

## 2. Objetivos y alcance funcional

### 2.1 Objetivos

- Centralizar la gestión administrativa de comisiones y grupos de trabajo del organismo.
- Garantizar la **trazabilidad** completa de los cambios sobre miembros, cargos y actas.
- Producir documentación oficial (actas) en formatos estándar **PDF** y **Word**.
- Cumplir con los requisitos de **seguridad corporativa** mediante integración con el Directorio Activo.

### 2.2 Alcance funcional

| Área | Funcionalidades |
|------|-----------------|
| **Comisiones** | Crear, listar, buscar por nombre, ver detalle, gestionar miembros y cargos. |
| **Miembros** | Alta, listado, búsqueda por DNI/NIF, asignación a múltiples comisiones. |
| **Cargos** | Asignación, cambio de cargo con motivo, historial completo de cargos previos. |
| **Actas** | Crear actas con asistencias y justificaciones, adjuntar PDF, ver, imprimir, generar PDF/Word. |
| **Auditoría** | Listado y filtrado de acciones por usuario, entidad o resultado (EXITOSO, FALLIDO, DENEGADO, VALIDACION_ERROR). |
| **Seguridad** | Login LDAP/AD, sesión con timeout, control de acceso por rol y método HTTP. |

---

## 3. Stack tecnológico

### 3.1 Plataforma

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Java | 11 |
| Empaquetado | WAR (Maven) | — |
| Contenedor | Apache Tomcat | 9.x |
| Base de datos | PostgreSQL | 12+ (driver 42.7.7) |
| Connection pool | HikariCP | 5.1.0 |
| API web | Servlet 4.0 / JSP 2.3 / JSTL 1.2 | — |

### 3.2 Dependencias principales (`pom.xml`)

| Dependencia | Versión | Uso |
|-------------|---------|-----|
| `javax.servlet-api` | 4.0.1 | API de Servlets (provista por Tomcat). |
| `javax.servlet.jsp-api` | 2.3.3 | API JSP. |
| `jstl` | 1.2 | Tag library JSTL. |
| `postgresql` | 42.7.7 | Driver JDBC. |
| `HikariCP` | 5.1.0 | Pool de conexiones. |
| `pdfbox` | 2.0.30 | Generación dinámica de PDF. |
| `poi-ooxml` | 5.3.0 | Generación dinámica de Word .docx. |
| `jackson-databind` | 2.18.2 | Serialización JSON (DTOs y AJAX). |
| `hibernate-validator` | 6.2.5.Final | Bean Validation (JSR-380). |
| `javax.el` (Glassfish) | 3.0.0 | Soporte EL requerido por Hibernate Validator. |
| `junit-jupiter` | 5.11.4 | Pruebas unitarias (scope test). |
| `javaee-api` | 8.0.1 | Soporte JNDI/LDAP (scope provided). |

### 3.3 Frontend

- **Bootstrap 5.3.0** (CDN).
- **Bootstrap Icons 1.11.3** (CDN).
- CSS corporativo propio en `src/main/webapp/css/style.css` con variables CSS para los colores institucionales (`--aragon-rojo: #C1272D`, `--aragon-azul: #0d6efd`).
- Vistas en **JSP + JSTL**, sin SPA.

---

## 4. Arquitectura de la aplicación

La aplicación sigue una arquitectura **MVC clásica en 4 capas** con separación clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────────┐
│                        Navegador                            │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/HTTPS
┌───────────────────────────▼─────────────────────────────────┐
│ Filtros: AuthFilter → RolFilter                             │
│ (autenticación + autorización por rol y método HTTP)        │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│ Controladores (Servlets)                                    │
│ LoginServlet, LogoutServlet, IndexServlet, ComisionController│
│ ActaController, MiembroController, CambiarCargoServlet,     │
│ AuditoriaController                                         │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│ Servicios                                                   │
│ ComisionService, MiembroService, ActaGeneratorService,      │
│ AuditoriaService, LdapAuthService, RolService               │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│ DAOs                                                        │
│ ComisionDAO, MiembroDAO, ComisionMiembroDAO, ActaDAO,       │
│ HistorialCargoDAO, AuditoriaDAO                             │
└───────────────────────────┬─────────────────────────────────┘
                            │ JDBC (HikariCP)
┌───────────────────────────▼─────────────────────────────────┐
│              PostgreSQL (JNDI: jdbc/Comisiones)             │
└─────────────────────────────────────────────────────────────┘
```

### 4.1 Capas

- **Filtros (`com.comisiones.filter`)** — `AuthFilter` exige sesión válida; `RolFilter` verifica permisos por ruta y método HTTP, redirigiendo a `sin-permisos.jsp` cuando procede.
- **Controladores (`com.comisiones.controller`)** — Servlets que reciben peticiones HTTP, validan parámetros, invocan servicios/DAOs y redirigen a vistas JSP.
- **Servicios (`com.comisiones.service`)** — Lógica de negocio (validaciones de duplicados, generación de documentos, registro de auditoría, autenticación LDAP).
- **DAOs (`com.comisiones.dao`)** — Acceso a datos con `PreparedStatement` y transacciones explícitas (`saveActaConAsistencias` es atómica).
- **Modelo (`com.comisiones.model`)** — POJOs `Acta`, `Comision`, `Miembro`, `ComisionMiembro`, `HistorialCargo`, `AsistenciaActa`, `UsuarioAD`, `AuditoriaAccion`, anotados con Bean Validation (`@NotNull`, `@PastOrPresent`, `@Size`).
- **DTOs (`com.comisiones.dto`)** — Objetos de transferencia para serialización JSON.
- **Utilidades (`com.comisiones.util`)** — `DBUtil` (HikariCP+JNDI), `AppLogger`, `AppConstants`, `DateFormatUtil`, `ServletHelper`.
- **Seguridad (`com.comisiones.security`, `com.comisiones.ldap`)** — `LdapAuthService`, `RolService`, `AppRoles`.

---

## 5. Estructura del repositorio

```
FormularioDeComisiones/
├── pom.xml                          # Configuración Maven (Java 11, WAR)
├── nb-configuration.xml             # Configuración NetBeans
├── .gitignore
├── database/
│   └── migrations/                  # Scripts SQL versionados (V1..V4 + 00X_)
│       ├── V1__esquema_inicial.sql
│       ├── 001_mejoras_criticas.sql
│       ├── V2__add_pdf_support.sql
│       ├── 002_historial_cargos.sql
│       ├── 002_fix_duplicate_indexes.sql
│       ├── 003_agregar_area_mixta.sql
│       ├── 003_fix_historial_cargo_constraints.sql
│       ├── V3__updated_schema.sql
│       ├── V4__cargo_enum.sql
│       ├── 003_trigger_usuario_ad.sql
│       ├── 004_auditoria_acciones.sql
│       ├── 005_fix_historial_cargo_types.sql
│       ├── 006_extend_auditoria_acciones.sql
│       └── README.md
├── docs/
│   ├── DATASOURCE_JNDI.md           # Configuración del DataSource Tomcat
│   ├── DATASOURCE_JNDI_Version1_OBSOLETO.md
│   ├── GENERACION_DOCUMENTOS.md     # Documentación PDF/Word
│   ├── context.xml.example          # Plantilla context.xml
│   └── DOSSIER.md                   # (este documento)
└── src/
    ├── main/
    │   ├── java/com/comisiones/
    │   │   ├── controller/          # Servlets
    │   │   ├── service/             # Lógica de negocio
    │   │   ├── dao/                 # Acceso a datos
    │   │   ├── model/               # POJOs de dominio
    │   │   ├── dto/                 # Objetos de transferencia
    │   │   ├── filter/              # Filtros HTTP (Auth, Rol)
    │   │   ├── security/            # Roles y autorización
    │   │   ├── ldap/                # Autenticación LDAP/AD
    │   │   └── util/                # Utilidades (DBUtil, AppLogger, ...)
    │   └── webapp/
    │       ├── WEB-INF/views/       # Vistas JSP (auth, comisiones, actas, auditoria, miembros)
    │       ├── META-INF/context.xml # Configuración JNDI (Tomcat)
    │       ├── css/style.css        # Estilos corporativos
    │       └── index.jsp            # Menú principal
    └── test/                        # Tests unitarios (JUnit 5)
```

---

## 6. Modelo de datos

### 6.1 Tipos ENUM (PostgreSQL)

```sql
CREATE TYPE area_type AS ENUM ('ATENCION_ESPECIALIZADA', 'ATENCION_PRIMARIA', 'MIXTA');
CREATE TYPE tipo_type AS ENUM ('COMISION', 'GRUPO_TRABAJO', 'GRUPO_MEJORA');
CREATE TYPE cargo_type AS ENUM (
    'REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'PARTICIPANTE',
    'SECRETARIO', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR'
);
```

### 6.2 Tablas principales

| Tabla | Descripción |
|-------|-------------|
| `comisiones` | Comisión / grupo de trabajo / grupo de mejora. PK BIGSERIAL. Constraint UNIQUE (nombre, area, tipo). |
| `miembros` | Personas. `dni_nif` UNIQUE, `correo_electronico` validado por regex. |
| `comision_miembros` | Relación N:M con `cargo`, `fecha_incorporacion` y `fecha_baja`. PK compuesta. |
| `actas` | Actas de reunión vinculadas a una comisión. Soporta PDF adjunto (`pdf_contenido BYTEA`, `pdf_nombre`, `pdf_tipo_mime`). |
| `asistencias_actas` | Asistencia de cada miembro a cada acta + `justificacion` opcional. |
| `comision_miembro_historial_cargos` | Historial completo de cambios de cargo (cargo anterior, nuevo, motivo, usuario, fecha). |
| `auditoria_acciones` | Auditoría centralizada con IP origen, user-agent, resultado, duración, mensaje de error, ID de sesión. |

### 6.3 Diagrama lógico (simplificado)

```
comisiones 1───N comision_miembros N───1 miembros
     │                    │
     │                    └──────N comision_miembro_historial_cargos
     │
     1
     │
     N
   actas 1───N asistencias_actas N───1 miembros

auditoria_acciones  (tabla independiente, registra acciones de cualquier entidad)
```

### 6.4 Integridad referencial y constraints

- `ON DELETE CASCADE` entre `comisiones → comision_miembros` y `comisiones → actas`.
- `CONSTRAINT check_fecha_reunion CHECK (fecha_reunion <= CURRENT_DATE)`.
- `CONSTRAINT check_email CHECK (correo_electronico ~* '^[A-Za-z0-9._%+-]+@...$')`.
- `CONSTRAINT check_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_constitucion)`.
- Todas las IDs son `BIGINT` (escalabilidad asegurada por migración `001`).

---

## 7. Módulos funcionales

### 7.1 Comisiones (`ComisionController` → `/comisiones/*`)

| Ruta | Método | Rol mínimo | Descripción |
|------|--------|-----------|-------------|
| `/comisiones` o `/comisiones/list` | GET | LECTURA | Listar todas las comisiones. |
| `/comisiones/buscarPorDni` | GET/POST | LECTURA | Buscar comisiones por DNI/NIF de un miembro. |
| `/comisiones/buscarComision` | GET/POST | LECTURA | Buscar por nombre. |
| `/comisiones/view/{id}` | GET | LECTURA | Detalle con miembros y cargos. |
| `/comisiones/new` | GET/POST | ADMIN | Crear nueva comisión. |
| `/comisiones/addMember/{id}` | GET/POST | GESTOR | Añadir miembro a comisión. |
| `/comisiones/bajaMiembros/{id}` | GET/POST | GESTOR | Dar de baja miembros. |
| `/comisiones/cambiarCargo` | GET/POST | GESTOR | Cambiar cargo y registrar historial. |
| `/comisiones/existentes` | GET | LECTURA | Endpoint AJAX/JSON. |

### 7.2 Miembros (`MiembroController` → `/miembros`)

Listado paginable de todos los miembros del sistema, ordenado alfabéticamente.

### 7.3 Actas (`ActaController` → `/actas/*`)

| Ruta | Método | Rol mínimo | Descripción |
|------|--------|-----------|-------------|
| `/actas/new` | GET | GESTOR | Mostrar formulario de creación. |
| `/actas/save` | POST | GESTOR | Guardar acta + asistencias (transacción atómica). |
| `/actas/view?id=N` | GET | LECTURA | Ver detalle del acta. |
| `/actas/loadMiembros` | GET | LECTURA | AJAX — carga miembros de comisión seleccionada. |
| `/actas/download-pdf?id=N` | GET | LECTURA | Descarga el PDF adjunto. |
| `/actas/view-pdf?id=N` | GET | LECTURA | Visualiza el PDF en el navegador. |
| `/actas/generate-pdf?id=N` | GET | LECTURA | **Genera dinámicamente un PDF** del acta (PDFBox). |
| `/actas/generate-word?id=N` | GET | LECTURA | **Genera dinámicamente un .docx** del acta (POI). |

Configurado con `@MultipartConfig` para subida de PDF adjunto con límites definidos en `AppConstants.MAX_PDF_SIZE` / `MAX_REQUEST_SIZE`.

### 7.4 Auditoría (`AuditoriaController` → `/auditoria`)

Listado de las **últimas 200 acciones** con filtros:

- `?usuario=xxx` → por usuario AD.
- `?entidad=COMISION&entidadId=123` → por entidad afectada.
- `?resultado=EXITOSO|FALLIDO|DENEGADO|VALIDACION_ERROR` → por resultado.

---

## 8. Seguridad y control de accesos

### 8.1 Autenticación

- `LoginServlet` (`/login`) lee desde JNDI los parámetros LDAP (`ldap/url`, `ldap/baseDn`, `ldap/dominio`, `ldap/bindDn`, `ldap/bindPassword`).
- `LdapAuthService` valida las credenciales contra **Active Directory**.
- Sesión HTTP con timeout de **30 minutos** (`SESSION_MAX_INACTIVE = 30 * 60`).
- `LogoutServlet` (`/logout`) invalida la sesión.
- Protección frente a *open redirect*: `esSafeRedirect()` rechaza URLs absolutas y protocolos relativos.

### 8.2 Autorización por roles

Los grupos AD se mapean a roles internos mediante JNDI (`roles/admin`, `roles/gestor`, `roles/lectura`).

| Rol interno | Permisos |
|-------------|----------|
| `ADMIN` | Acceso total (crear/editar comisiones, todo lo del gestor). |
| `GESTOR` | Gestión de miembros y actas (añadir, baja, cambiar cargo, crear actas). |
| `LECTURA` | Solo lectura: consulta de comisiones, miembros, actas, generación de documentos. |
| *(sin rol)* | Redirección a `sin-permisos.jsp`. |

Prioridad de resolución: **ADMIN > GESTOR > LECTURA**. Cualquier usuario autenticado obtiene al menos `LECTURA` como *fallback*.

### 8.3 Filtros HTTP

- `AuthFilter` — Verifica la sesión; redirige a `/login` si no existe.
- `RolFilter` — Aplica reglas de autorización por **ruta + método HTTP**. Rutas públicas: `/login`, `/logout`, recursos estáticos (`/css/`, `/js/`, `/img/`, etc.).

### 8.4 Buenas prácticas implementadas

- **Sin credenciales en código**: todas las credenciales (BD, LDAP, grupos AD) vienen de `context.xml` vía JNDI.
- **PreparedStatements** en todos los DAO → protección contra SQL injection.
- **Bean Validation** (JSR-380) en modelos críticos (`Acta`, etc.).
- **Sesión** invalidada en logout explícito.
- **CSRF**: actualmente sin token explícito (mejora futura recomendada).

---

## 9. Generación dinámica de documentos (PDF y Word)

Servicio: `com.comisiones.service.ActaGeneratorService`.

### 9.1 PDF (Apache PDFBox 2.0.30)

- Tamaño de página: **A4**.
- Fuente: **Helvetica** (estándar PDF).
- Tamaños: Título 18pt bold, subtítulos 14pt bold, texto 12pt.
- Márgenes de 50 puntos.
- Tabla de asistencias con 4 columnas: Nombre, DNI, Asistencia, Justificación.
- Pie de página con fecha de generación (10pt).
- **TODO conocido:** migración futura a PDFBox 3.x (la API de `PDType1Font` cambia).

### 9.2 Word .docx (Apache POI 5.3.0)

- Documento Office Open XML.
- Título centrado 18pt en negrita.
- Tabla de asistencias estructurada con bordes.
- Pie alineado a la derecha, itálica, 10pt.

### 9.3 Flujo

```
Usuario → /actas/view?id=N
       → clic en "Generar PDF" o "Generar Word"
       → GET /actas/generate-pdf?id=N  (o /generate-word)
       → ActaController valida el ID y carga acta + asistencias
       → ActaGeneratorService.generarPdf(...) / generarWord(...)
       → byte[] en memoria
       → Response con Content-Type y Content-Disposition: attachment
       → Descarga Acta_{id}.pdf / Acta_{id}.docx
```

> Los documentos se generan **íntegramente en memoria** (`ByteArrayOutputStream`), sin archivos temporales en disco.

---

## 10. Auditoría y trazabilidad

### 10.1 Servicio centralizado

`AuditoriaService` (singleton) inserta cada acción en `auditoria_acciones`. **Las excepciones NO se propagan**: si la auditoría falla, la operación de negocio continúa, garantizando que la auditoría nunca rompa la funcionalidad del usuario.

### 10.2 Campos registrados

- `fecha_hora`, `usuario` (AD username o "SISTEMA")
- `accion`: `CREAR`, `MODIFICAR`, `ELIMINAR`, `BAJA`, `LOGIN`, `LOGOUT`, `LOGIN_FALLIDO`, `ACCESS_DENIED`
- `entidad`: `COMISION`, `MIEMBRO`, `ACTA`, `CARGO`, `SESION`
- `entidad_id`, `descripcion`
- `ip_origen` (con soporte de cabeceras de proxy), `user_agent`, `sesion_id`
- `resultado`: `EXITOSO` / `FALLIDO` / `DENEGADO` / `VALIDACION_ERROR`
- `duracion_ms`, `mensaje_error`

### 10.3 Historial de cargos

Toda alteración de cargo en `comision_miembros` deja constancia en `comision_miembro_historial_cargos` con `cargo_anterior`, `cargo_nuevo`, `motivo` y `usuario_modificacion` (a través del trigger `003_trigger_usuario_ad.sql`).

---

## 11. Configuración y despliegue

### 11.1 Requisitos

- **JDK 11**
- **Apache Tomcat 9** con el **driver JDBC PostgreSQL** disponible en `$CATALINA_HOME/lib/` o dentro del WAR.
- **PostgreSQL 12+** con base de datos `comisiones` creada.
- Acceso a un servidor **LDAP / Active Directory**.

### 11.2 Configuración (`context.xml`)

El archivo `src/main/webapp/META-INF/context.xml` (ver plantilla en `docs/context.xml.example`) define:

```xml
<Context>
  <Resource name="jdbc/Comisiones"
            auth="Container"
            type="javax.sql.DataSource"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost:5432/comisiones"
            username="CHANGE_ME" password="CHANGE_ME"
            maxActive="10" maxIdle="5" minIdle="2"
            maxWaitMillis="30000"
            testOnBorrow="true" validationQuery="SELECT 1" />

  <!-- LDAP -->
  <Environment name="ldap/url"        value="ldap://dc.empresa.local:389" type="java.lang.String"/>
  <Environment name="ldap/baseDn"     value="DC=empresa,DC=local"         type="java.lang.String"/>
  <Environment name="ldap/dominio"    value="empresa.local"               type="java.lang.String"/>
  <Environment name="ldap/bindDn"     value="..."                         type="java.lang.String"/>
  <Environment name="ldap/bindPassword" value="..."                       type="java.lang.String"/>

  <!-- Mapeo de grupos AD a roles internos -->
  <Environment name="roles/admin"   value="CN=Comisiones_Admin,..."   type="java.lang.String"/>
  <Environment name="roles/gestor"  value="CN=Comisiones_Gestor,..."  type="java.lang.String"/>
  <Environment name="roles/lectura" value="CN=Comisiones_Lectura,..." type="java.lang.String"/>
</Context>
```

> En **producción** se recomienda colocar el `context.xml` fuera del WAR, en `$CATALINA_HOME/conf/Catalina/localhost/FormularioDeComisiones.xml`, para poder modificar credenciales sin reconstruir el artefacto.

### 11.3 Build y despliegue

```bash
# Compilar y empaquetar
mvn clean package

# Resultado: target/FormularioDeComisiones-1.0-SNAPSHOT.war
# Renombrar y copiar a Tomcat
cp target/FormularioDeComisiones-1.0-SNAPSHOT.war \
   $CATALINA_HOME/webapps/FormularioDeComisiones.war

# Tomcat lo desplegará automáticamente
```

### 11.4 Connection pool (HikariCP)

`DBUtil` configura el pool de forma programática a partir de los valores JNDI:

```
maximumPoolSize    = 10
minimumIdle        = 2
connectionTimeout  = 30 000 ms
idleTimeout        = 600 000 ms
maxLifetime        = 1 800 000 ms
cachePrepStmts     = true
prepStmtCacheSize  = 250
prepStmtCacheSqlLimit = 2048
```

---

## 12. Migraciones de base de datos

Las migraciones están versionadas en `database/migrations/` y deben ejecutarse en orden:

| Orden | Archivo | Descripción |
|-------|---------|-------------|
| 1 | `V1__esquema_inicial.sql` | Esquema inicial completo. |
| 2 | `001_mejoras_criticas.sql` | Correcciones críticas, BIGINT, auditoría inicial. |
| 3 | `V2__add_pdf_support.sql` | Soporte para PDFs adjuntos en actas. |
| 4 | `002_historial_cargos.sql` | Tabla de historial de cargos. |
| 5 | `002_fix_duplicate_indexes.sql` | Eliminación de índices duplicados. |
| 6 | `003_agregar_area_mixta.sql` | Valor `MIXTA` en enum `area_type`. |
| 7 | `003_fix_historial_cargo_constraints.sql` | Constraints de validación en historial. |
| 8 | `V3__updated_schema.sql` | Esquema consolidado. |
| 9 | `V4__cargo_enum.sql` | Conversión de `cargo` a ENUM `cargo_type`. |
| 10 | `003_trigger_usuario_ad.sql` | Trigger para usuario AD en historial. |
| 11 | `004_auditoria_acciones.sql` | Tabla `auditoria_acciones`. |
| 12 | `005_fix_historial_cargo_types.sql` | Cargos del historial como ENUM. |
| 13 | `006_extend_auditoria_acciones.sql` | Extensión: user_agent, resultado, duracion_ms, mensaje_error, sesion_id. |

> Las migraciones futuras deberán seguir el formato `NNN_descripcion.sql`.

---

## 13. Calidad, pruebas y observabilidad

### 13.1 Pruebas

- Framework: **JUnit 5** (`junit-jupiter` 5.11.4).
- Plugin Surefire 3.5.2.
- Tests en `src/test/`.

### 13.2 Logging

Logger ligero propio (`AppLogger`):

- `info`, `error`, `debug`, `separator`.
- `DEBUG_MODE` controlado por constante (cambiar a `true` en desarrollo).
- Salida a `stdout`/`stderr` para integración con `catalina.out`.

### 13.3 Validación

- **Bean Validation (Hibernate Validator)** en modelos (`@NotNull`, `@PastOrPresent`, `@Size`).
- Validación de DNI/NIF, email y fechas en BBDD mediante CHECK constraints.

### 13.4 Internacionalización

- Aplicación íntegramente en **español (es-ES)**.
- Formato de fecha: `dd/MM/yyyy` (`DateFormatUtil`).

---

## 14. Mejoras futuras y roadmap

Priorizadas por impacto:

1. **Migración a PDFBox 3.x** (TODO declarado en `pom.xml`).
2. **CSRF tokens** en formularios POST.
3. **Rate-limiting** en generación de documentos.
4. **Plantillas personalizables** (logo, firmas digitales).
5. **Generación asíncrona** para actas con gran volumen de asistencias.
6. **Más formatos**: ODT, HTML, RTF.
7. **Exportación por lotes** (ZIP con varias actas).
8. **Internacionalización (i18n)** completa.
9. **Tests de integración** con Testcontainers (PostgreSQL + Tomcat embedded).
10. **Pipeline CI/CD** (GitHub Actions) — actualmente no presente.

---

## 15. Anexos

### 15.1 Variables JNDI utilizadas

| Clave JNDI | Tipo | Uso |
|------------|------|-----|
| `jdbc/Comisiones` | DataSource | Conexión a PostgreSQL (consumida por HikariCP). |
| `db/url`, `db/username`, `db/password` | String | Credenciales BD (alternativa programática). |
| `ldap/url`, `ldap/baseDn`, `ldap/dominio` | String | Conexión LDAP. |
| `ldap/bindDn`, `ldap/bindPassword` | String | Bind opcional (fallback a credenciales del usuario). |
| `roles/admin`, `roles/gestor`, `roles/lectura` | String | Mapeo grupos AD → roles internos. |

### 15.2 Endpoints principales

| Método | URL | Rol |
|--------|-----|-----|
| GET/POST | `/login` | público |
| GET | `/logout` | autenticado |
| GET | `/` o `/index` | LECTURA |
| GET | `/comisiones` | LECTURA |
| GET | `/comisiones/view/{id}` | LECTURA |
| GET/POST | `/comisiones/new` | ADMIN |
| GET/POST | `/comisiones/addMember/{id}` | GESTOR |
| GET/POST | `/comisiones/bajaMiembros/{id}` | GESTOR |
| GET/POST | `/comisiones/cambiarCargo` | GESTOR |
| GET | `/miembros` | LECTURA |
| GET | `/actas/new` | GESTOR |
| POST | `/actas/save` | GESTOR |
| GET | `/actas/view?id=N` | LECTURA |
| GET | `/actas/generate-pdf?id=N` | LECTURA |
| GET | `/actas/generate-word?id=N` | LECTURA |
| GET | `/auditoria` | ADMIN |

### 15.3 Glosario

- **Acta** — Documento que recoge una reunión de una comisión, con asistencias y observaciones.
- **Cargo** — Rol funcional de un miembro en una comisión (PRESIDENTE, SECRETARIO, etc.).
- **Comisión / Grupo de Trabajo / Grupo de Mejora** — Tipos de entidad organizativa gestionados.
- **JNDI** — Servicio de directorio de Java EE usado por Tomcat para inyectar recursos.
- **HikariCP** — Pool de conexiones JDBC de alto rendimiento.
- **AD / LDAP** — Active Directory accedido por LDAP para autenticación.

### 15.4 Referencias internas

- [`docs/DATASOURCE_JNDI.md`](./DATASOURCE_JNDI.md) — Configuración detallada del DataSource.
- [`docs/GENERACION_DOCUMENTOS.md`](./GENERACION_DOCUMENTOS.md) — Documentación extendida de generación PDF/Word.
- [`docs/context.xml.example`](./context.xml.example) — Plantilla de configuración Tomcat.
- [`database/migrations/README.md`](../database/migrations/README.md) — Detalle de migraciones SQL.

---

*Dossier generado para el repositorio `Ettorinho/FormularioDeComisiones` — rama `main`.*
