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
