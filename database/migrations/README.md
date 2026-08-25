# Migraciones de base de datos

Scripts SQL ordenados por versión, siguiendo la convención de nombrado **Flyway** (`V<N>__<descripcion>.sql`).

## 📐 Convención adoptada

Todos los scripts de migración siguen el formato:

```
V<N>__<descripcion>.sql
```

- `V` — prefijo obligatorio (letra mayúscula V)
- `<N>` — número de versión entero, empieza en 1, sin ceros a la izquierda
- `__` — doble guión bajo como separador
- `<descripcion>` — descripción corta en minúsculas con guiones bajos

Los scripts de rollback se almacenan en **`database/rollbacks/`** (separados de las migraciones) y **no son escaneados por Flyway** para evitar conflictos de versión duplicada.

## ⚠️ Estrategia de baseline: V1 vs V3

Este repositorio tiene dos archivos que definen un esquema "completo":

- **`V1__esquema_inicial.sql`** — dump `pg_dump` del esquema original. Es el baseline histórico para instalaciones que apliquen la secuencia completa V1→Vn.
- **`V3__updated_schema.sql`** — esquema completo actualizado que incluye todos los tipos ENUM y tablas en su forma actual. Es un **baseline alternativo** pensado para instalaciones nuevas que quieran partir de un estado limpio sin necesidad de aplicar V1 y V2.

> ⚠️ **V3 NO es incremental respecto a V2.** Si aplicas V1→V2→V3 en ese orden (Flyway por defecto), V3 intentará recrear objetos ya existentes. Usa `IF NOT EXISTS` donde es posible, pero puede generar advertencias o conflictos. Para instalaciones nuevas, elige **Opción B** en la sección de instalación.
>
> **Bases de datos existentes en producción**: no reescribir el historial de Flyway. Si V1, V2 y V3 ya están aplicadas y registradas en `flyway_schema_history`, las siguientes migraciones (V4 en adelante) son puramente incrementales y no presentan conflicto.

## 📋 Migraciones disponibles

| Versión | Archivo | Descripción | Estado |
|---------|---------|-------------|--------|
| V1 | `V1__esquema_inicial.sql` | Esquema original de la base de datos (dump pg_dump) | Baseline histórico |
| V2 | `V2__add_pdf_support.sql` | Soporte para almacenamiento de PDFs en actas | Incremental |
| V3 | `V3__updated_schema.sql` | Esquema actualizado completo (incluye todos los tipos ENUM, tablas y constraints actualizados). Ver nota de baseline arriba. | Baseline alternativo |
| V4 | `V4__cargo_enum_fixed.sql` | Convierte columna `cargo` de VARCHAR a ENUM `cargo_type`. Maneja correctamente el trigger existente (drop + recreate). **Reemplaza** la versión original rota. | Incremental |
| V5 | `V5__mejoras_criticas.sql` | Renombra tabla asistencias, conversión de IDs a BIGINT, triggers de auditoría, constraints de validación | Incremental |
| V6 | `V6__historial_cargos.sql` | Crea tabla `comision_miembro_historial_cargos` con trigger automático de auditoría de cambios de cargo | Incremental |
| V7 | `V7__fix_duplicate_indexes.sql` | Elimina índices duplicados en tablas `actas` y `miembros` | Incremental |
| V8 | `V8__agregar_area_mixta.sql` | Agrega el valor `MIXTA` al enum `area_type` (idempotente) | Incremental |
| V9 | `V9__fix_historial_cargo_constraints.sql` | Añade CHECK constraints a `comision_miembro_historial_cargos` | Incremental |
| V10 | `V10__trigger_usuario_ad.sql` | Trigger para propagar `app.usuario_modificacion` al historial de cargos | Incremental |
| V11 | `V11__auditoria_acciones.sql` | Crea tabla `auditoria_acciones` para registro centralizado de auditoría | Incremental |
| V12 | `V12__fix_historial_cargo_types.sql` | Convierte `cargo_anterior`/`cargo_nuevo` de VARCHAR a `cargo_type` ENUM en historial | Incremental |
| V13 | `V13__extend_auditoria_acciones.sql` | Añade columnas de seguridad a `auditoria_acciones` (user_agent, resultado, duracion_ms, etc.) | Incremental |
| V14 | `V14__add_titulo_to_actas.sql` | Añade columna `titulo` (VARCHAR 200) a la tabla `actas` | Incremental |
| V15 | `V15__add_cargo_firmante.sql` | Añade valor `FIRMANTE` al ENUM `cargo_type` y actualiza CHECK constraints | Incremental |
| V16 | `V16__fix_trigger_registrar_cambio_cargo.sql` | Corrige el trigger `registrar_cambio_cargo` en `comision_miembros` | Incremental |
| V17 | `V17__add_auditoria_indexes.sql` | Añade índices sobre `auditoria_acciones` | Incremental |
| V18 | `V18__expand_varchar_columns.sql` | Amplía `nombre` (comisiones) y `nombre_apellidos` (miembros) a VARCHAR(200) para alinear con validaciones Java | Incremental |
| V19 | `V19__add_miembro_id_index.sql` | Índice parcial en `comision_miembros(miembro_id)` para miembros activos | Incremental |
| V20 | `V20__add_comisiones_lower_nombre_index.sql` | Índice funcional en `LOWER(nombre)` para búsquedas case-insensitive | Incremental |

## ⚠️ Nota sobre V4: resolución del conflicto de versión duplicada

Existían dos archivos con versión V4:
- `V4__cargo_enum.sql` — versión original, **fallaba** con el error "no se puede alterar el tipo de una columna usada en trigger"
- `V4__cargo_enum _fixed.sql` — versión corregida (tenía un espacio en el nombre)

**Resolución**: Se eliminó el archivo roto (`V4__cargo_enum.sql`) y se renombró el corregido a `V4__cargo_enum_fixed.sql`. Este es ahora el único V4.

Si tu base de datos ya tiene aplicado el V4 original (y falló), aplica V4__cargo_enum_fixed.sql manualmente y actualiza el checksum en `flyway_schema_history`.

## 🔄 Rutas de instalación

### Instalación nueva (desde cero)
```bash
# Opción A: Flyway aplica V1 → V20 en orden (incluye V3 como incremental con posibles advertencias)
flyway migrate

# Opción B (recomendada para instalaciones nuevas): Usar V3 como baseline limpio + V4 en adelante
flyway baseline -baselineVersion=3
flyway migrate
```

### Actualización de instalación existente
```bash
# Flyway aplica solo las versiones no registradas en flyway_schema_history
flyway migrate
```

### Ejecución manual (sin Flyway)
```sql
-- Ejecutar en orden:
\i V1__esquema_inicial.sql
\i V2__add_pdf_support.sql
-- (o usar V3 como base completa + continuar desde V4)
\i V4__cargo_enum_fixed.sql
\i V5__mejoras_criticas.sql
\i V6__historial_cargos.sql
\i V7__fix_duplicate_indexes.sql
\i V8__agregar_area_mixta.sql
\i V9__fix_historial_cargo_constraints.sql
\i V10__trigger_usuario_ad.sql
\i V11__auditoria_acciones.sql
\i V12__fix_historial_cargo_types.sql
\i V13__extend_auditoria_acciones.sql
\i V14__add_titulo_to_actas.sql
\i V15__add_cargo_firmante.sql
\i V16__fix_trigger_registrar_cambio_cargo.sql
\i V17__add_auditoria_indexes.sql
\i V18__expand_varchar_columns.sql
\i V19__add_miembro_id_index.sql
\i V20__add_comisiones_lower_nombre_index.sql
```

## ⏪ Rollbacks disponibles

Los rollbacks se almacenan en **`database/rollbacks/`** (fuera del directorio de migraciones para que Flyway no los escanee). **NO son migraciones Flyway** y deben ejecutarse manualmente si se necesita revertir:

| Script | Revierte |
|--------|----------|
| `V5__rollback.sql` | Migración V5 (mejoras críticas) |
| `V6__rollback.sql` | Migración V6 (historial cargos) |
| `V8__rollback.sql` | Migración V8 (área MIXTA — solo si no hay datos) |

> ⚠️ **ADVERTENCIA**: Algunos rollbacks contienen operaciones `CASCADE`. Revisa el script antes de ejecutar y asegúrate de tener un backup.

## 📝 Registro de migraciones

Una vez aplicadas mediante Flyway, el historial se almacena automáticamente en la tabla `flyway_schema_history`.

Para consultar el estado:
```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## 🔒 Antes de ejecutar cualquier migración

1. **Backup**: `pg_dump -U postgres -d nombre_bd > backup_$(date +%Y%m%d_%H%M%S).sql`
2. **Entorno de prueba**: validar en staging antes de producción
3. **Verificar estado**: revisar `flyway_schema_history` para conocer la versión actual

---

**Convención adoptada**: Flyway `V<N>__<descripcion>.sql`  
**Última actualización**: 2026-08-25


Scripts SQL ordenados por versión, siguiendo la convención de nombrado **Flyway** (`V<N>__<descripcion>.sql`).

## 📐 Convención adoptada

Todos los scripts de migración siguen el formato:

```
V<N>__<descripcion>.sql
```

- `V` — prefijo obligatorio (letra mayúscula V)
- `<N>` — número de versión entero, empieza en 1, sin ceros a la izquierda
- `__` — doble guión bajo como separador
- `<descripcion>` — descripción corta en minúsculas con guiones bajos

Los scripts de rollback (no aplicados por Flyway) siguen el formato:

```
V<N>__rollback.sql
```

## 📋 Migraciones disponibles

| Versión | Archivo | Descripción | Estado |
|---------|---------|-------------|--------|
| V1 | `V1__esquema_inicial.sql` | Esquema original de la base de datos (dump pg_dump) | Baseline |
| V2 | `V2__add_pdf_support.sql` | Soporte para almacenamiento de PDFs en actas | Incremental |
| V3 | `V3__updated_schema.sql` | Esquema actualizado completo (incluye todos los tipos ENUM, tablas y constraints actualizados). Usar para instalaciones nuevas. | Baseline alternativo |
| V4 | `V4__cargo_enum_fixed.sql` | Convierte columna `cargo` de VARCHAR a ENUM `cargo_type`. Maneja correctamente el trigger existente (drop + recreate). **Reemplaza** la versión original rota. | Incremental |
| V5 | `V5__mejoras_criticas.sql` | Renombra tabla asistencias, conversión de IDs a BIGINT, triggers de auditoría, constraints de validación | Incremental |
| V6 | `V6__historial_cargos.sql` | Crea tabla `comision_miembro_historial_cargos` con trigger automático de auditoría de cambios de cargo | Incremental |
| V7 | `V7__fix_duplicate_indexes.sql` | Elimina índices duplicados en tablas `actas` y `miembros` | Incremental |
| V8 | `V8__agregar_area_mixta.sql` | Agrega el valor `MIXTA` al enum `area_type` (idempotente) | Incremental |
| V9 | `V9__fix_historial_cargo_constraints.sql` | Añade CHECK constraints a `comision_miembro_historial_cargos` | Incremental |
| V10 | `V10__trigger_usuario_ad.sql` | Trigger para propagar `app.usuario_modificacion` al historial de cargos | Incremental |
| V11 | `V11__auditoria_acciones.sql` | Crea tabla `auditoria_acciones` para registro centralizado de auditoría | Incremental |
| V12 | `V12__fix_historial_cargo_types.sql` | Convierte `cargo_anterior`/`cargo_nuevo` de VARCHAR a `cargo_type` ENUM en historial | Incremental |
| V13 | `V13__extend_auditoria_acciones.sql` | Añade columnas de seguridad a `auditoria_acciones` (user_agent, resultado, duracion_ms, etc.) | Incremental |
| V14 | `V14__add_titulo_to_actas.sql` | Añade columna `titulo` (VARCHAR 200) a la tabla `actas` | Incremental |

## ⚠️ Nota sobre V4: resolución del conflicto de versión duplicada

Existían dos archivos con versión V4:
- `V4__cargo_enum.sql` — versión original, **fallaba** con el error "no se puede alterar el tipo de una columna usada en trigger"
- `V4__cargo_enum _fixed.sql` — versión corregida (tenía un espacio en el nombre)

**Resolución**: Se eliminó el archivo roto (`V4__cargo_enum.sql`) y se renombró el corregido a `V4__cargo_enum_fixed.sql`. Este es ahora el único V4.

Si tu base de datos ya tiene aplicado el V4 original (y falló), aplica V4__cargo_enum_fixed.sql manualmente y actualiza el checksum en `flyway_schema_history`.

## 🔄 Rutas de instalación

### Instalación nueva (desde cero)
```bash
# Opción A: Flyway aplica V1 → V14 en orden
flyway migrate

# Opción B: Usar V3 como baseline (esquema completo) + V4 en adelante
flyway baseline -baselineVersion=3
flyway migrate
```

### Actualización de instalación existente
```bash
# Flyway aplica solo las versiones no registradas en flyway_schema_history
flyway migrate
```

### Ejecución manual (sin Flyway)
```sql
-- Ejecutar en orden:
\i V1__esquema_inicial.sql
\i V2__add_pdf_support.sql
-- (o usar V3 como base completa + continuar desde V4)
\i V4__cargo_enum_fixed.sql
\i V5__mejoras_criticas.sql
\i V6__historial_cargos.sql
\i V7__fix_duplicate_indexes.sql
\i V8__agregar_area_mixta.sql
\i V9__fix_historial_cargo_constraints.sql
\i V10__trigger_usuario_ad.sql
\i V11__auditoria_acciones.sql
\i V12__fix_historial_cargo_types.sql
\i V13__extend_auditoria_acciones.sql
\i V14__add_titulo_to_actas.sql
```

## ⏪ Rollbacks disponibles

Los rollbacks **NO son migraciones Flyway** y deben ejecutarse manualmente si se necesita revertir:

| Script | Revierte |
|--------|----------|
| `V5__rollback.sql` | Migración V5 (mejoras críticas) |
| `V6__rollback.sql` | Migración V6 (historial cargos) |
| `V8__rollback.sql` | Migración V8 (área MIXTA — solo si no hay datos) |

> ⚠️ **ADVERTENCIA**: Algunos rollbacks contienen operaciones `CASCADE`. Revisa el script antes de ejecutar y asegúrate de tener un backup.

## 📝 Registro de migraciones

Una vez aplicadas mediante Flyway, el historial se almacena automáticamente en la tabla `flyway_schema_history`.

Para consultar el estado:
```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## 🔒 Antes de ejecutar cualquier migración

1. **Backup**: `pg_dump -U postgres -d nombre_bd > backup_$(date +%Y%m%d_%H%M%S).sql`
2. **Entorno de prueba**: validar en staging antes de producción
3. **Verificar estado**: revisar `flyway_schema_history` para conocer la versión actual

---

**Convención adoptada**: Flyway `V<N>__<descripcion>.sql`  
**Última actualización**: 2026-07-21
