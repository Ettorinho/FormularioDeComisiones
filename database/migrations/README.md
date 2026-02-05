# Database Migrations

Este directorio contiene los scripts de migración para la base de datos del Sistema de Gestión de Comisiones.

## 📋 Migraciones Disponibles

### **001_mejoras_criticas.sql** - Mejoras Críticas y Correcciones
**Fecha**: 2026-02-05  
**Estado**: Listo para ejecutar  
**Reversible**: Sí (ver 001_rollback.sql)

#### 🎯 Objetivos

Esta migración implementa correcciones críticas, mejoras de escalabilidad, auditoría y validaciones de integridad.

#### 📊 Cambios Incluidos

##### 1️⃣ **CORRECCIONES CRÍTICAS** (Prioridad Alta)
- ✅ Renombra tabla `asistencias_acta` → `asistencias_actas` (consistencia con código Java)
- ✅ Añade `PARTICIPANTE` y otros cargos al constraint de validación (`check_cargo`)

##### 2️⃣ **MEJORAS DE ESCALABILIDAD** (Prioridad Media)
- ✅ Estandariza todos los IDs a `BIGINT` (comisiones, miembros, relaciones)
- ✅ Actualiza secuencias a `BIGINT`

##### 3️⃣ **MEJORAS DE AUDITORÍA** (Prioridad Media)
- ✅ Añade campos `fecha_creacion` y `fecha_modificacion` donde faltan
- ✅ Crea función `actualizar_fecha_modificacion()` para auto-actualización
- ✅ Implementa triggers automáticos en todas las tablas

##### 4️⃣ **VALIDACIONES DE INTEGRIDAD** (Prioridad Baja)
- ✅ Amplía `dni_nif` de VARCHAR(10) a VARCHAR(15) para NIE/pasaportes
- ✅ Añade validación de formato de email con regex
- ✅ Añade validación de `fecha_reunion` (no permite fechas futuras)

---

## 🚀 Instrucciones de Ejecución

### Pre-requisitos

1. **PostgreSQL 12 o superior** instalado
2. **Permisos de administrador** en la base de datos
3. **Backup completo** de la base de datos (OBLIGATORIO)

### Paso 1: Crear Backup

```bash
# Reemplazar 'nombre_bd' con el nombre real de tu base de datos
pg_dump -U postgres -d nombre_bd > backup_pre_migracion_$(date +%Y%m%d_%H%M%S).sql
```

**⚠️ IMPORTANTE**: Verifica que el backup se creó correctamente antes de continuar:

```bash
# Verificar que el archivo de backup no está vacío
ls -lh backup_pre_migracion_*.sql
```

### Paso 2: Ejecutar la Migración

```bash
# Opción 1: Desde línea de comandos
psql -U postgres -d nombre_bd -f database/migrations/001_mejoras_criticas.sql

# Opción 2: Desde psql interactivo
psql -U postgres -d nombre_bd
\i database/migrations/001_mejoras_criticas.sql
```

### Paso 3: Verificar la Migración

El script incluye verificaciones automáticas que se ejecutan al final. Revisa la salida para confirmar:

- ✓ Tabla `asistencias_actas` renombrada correctamente
- ✓ 5 triggers creados correctamente
- ✓ Constraints de validación creados

También puedes ejecutar estas verificaciones manualmente:

```sql
-- Verificar tabla renombrada
SELECT EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name = 'asistencias_actas'
);

-- Listar triggers creados
SELECT trigger_name, event_object_table 
FROM information_schema.triggers 
WHERE trigger_name LIKE 'trigger_%_modificacion'
ORDER BY event_object_table;

-- Verificar constraints
SELECT table_name, constraint_name, check_clause
FROM information_schema.check_constraints
WHERE constraint_schema = 'public'
  AND constraint_name IN ('check_cargo', 'check_email', 'check_fecha_reunion');
```

---

## ⏪ Rollback (Revertir Cambios)

Si necesitas revertir la migración, ejecuta el script de rollback:

### ⚠️ ADVERTENCIAS ANTES DE EJECUTAR ROLLBACK

1. **DNI/NIF**: Si algún miembro tiene un DNI/NIF con más de 10 caracteres, el rollback fallará al intentar revertir a VARCHAR(10). En ese caso, mantén VARCHAR(15).

2. **IDs grandes**: Si algún ID supera 2,147,483,647 (límite de INTEGER), el rollback fallará. En ese caso, mantén BIGINT.

3. **Triggers y auditoría**: Los campos `fecha_modificacion` dejarán de actualizarse automáticamente.

### Ejecutar Rollback

```bash
# Crear backup antes del rollback (por si acaso)
pg_dump -U postgres -d nombre_bd > backup_pre_rollback_$(date +%Y%m%d_%H%M%S).sql

# Ejecutar rollback
psql -U postgres -d nombre_bd -f database/migrations/001_rollback.sql
```

---

## 🧪 Testing Post-Migración

### 1. Verificar Integridad de Datos

```sql
-- Contar registros en todas las tablas
SELECT 'comisiones' AS tabla, COUNT(*) AS registros FROM comisiones
UNION ALL
SELECT 'miembros', COUNT(*) FROM miembros
UNION ALL
SELECT 'comision_miembros', COUNT(*) FROM comision_miembros
UNION ALL
SELECT 'actas', COUNT(*) FROM actas
UNION ALL
SELECT 'asistencias_actas', COUNT(*) FROM asistencias_actas;
```

### 2. Probar Triggers

```sql
-- Actualizar un miembro y verificar que fecha_modificacion cambia
UPDATE miembros SET correo_electronico = correo_electronico WHERE id = 1;
SELECT id, nombre_apellidos, fecha_modificacion FROM miembros WHERE id = 1;
```

### 3. Probar Validaciones

```sql
-- Intentar insertar email inválido (debe fallar)
INSERT INTO miembros (nombre_apellidos, dni_nif, correo_electronico) 
VALUES ('Test Usuario', '12345678X', 'email_invalido');

-- Intentar insertar fecha futura en acta (debe fallar)
INSERT INTO actas (comision_id, fecha_reunion, observaciones)
VALUES (1, CURRENT_DATE + INTERVAL '1 day', 'Prueba');

-- Insertar miembro con cargo PARTICIPANTE (debe funcionar)
INSERT INTO comision_miembros (comision_id, miembro_id, cargo, fecha_incorporacion)
VALUES (1, 1, 'PARTICIPANTE', CURRENT_DATE);
```

### 4. Verificar Referencias en Java

Asegúrate de que el código Java se ha actualizado para usar `asistencias_actas` (con 's'):

```bash
# Buscar referencias al nombre antiguo
grep -r "asistencias_acta" src/main/java/
```

---

## 📝 Registro de Migraciones Aplicadas

| Versión | Fecha Aplicación | Aplicado Por | Estado | Rollback Disponible |
|---------|------------------|--------------|--------|---------------------|
| 001     | YYYY-MM-DD       | [Tu Nombre]  | ✅ OK  | Sí                  |

---

## 🔒 Consideraciones de Seguridad

1. **Backups**: Siempre crea un backup antes de ejecutar migraciones
2. **Entorno de prueba**: Ejecuta primero en un ambiente de desarrollo/staging
3. **Horario**: Ejecuta migraciones en ventanas de mantenimiento programadas
4. **Monitoreo**: Supervisa el rendimiento de la BD después de la migración
5. **Validación**: Ejecuta tests completos después de la migración

---

## 📞 Soporte

Si encuentras problemas durante la migración:

1. **NO REINTENTAR** si hay errores críticos
2. Conserva el log de error completo
3. Verifica el estado actual de la BD con las consultas de verificación
4. Si es necesario, ejecuta el rollback
5. Contacta al equipo de desarrollo con:
   - Log completo del error
   - Resultado de las verificaciones
   - Versión de PostgreSQL
   - Estado de los datos (counts de tablas)

---

## 📚 Referencias

- **Esquema actual**: `esquema.sql`
- **Código Java**: `src/main/java/com/comisiones/`
- **Documentación PostgreSQL**: https://www.postgresql.org/docs/

---

## 🔄 Versionado

Este sistema de migraciones usa numeración secuencial:
- `001_*.sql` - Primera migración
- `002_*.sql` - Segunda migración (futura)
- etc.

Cada migración debe:
- ✅ Ser auto-documentada (comentarios claros)
- ✅ Incluir verificaciones post-ejecución
- ✅ Tener script de rollback correspondiente
- ✅ Preservar datos existentes
- ✅ Ser idempotente cuando sea posible (usar `IF EXISTS`, `IF NOT EXISTS`)

---

**Última actualización**: 2026-02-05  
**Versión**: 1.0
