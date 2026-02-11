# Migración 003: Área MIXTA

## Descripción

Agrega el área "MIXTA" al catálogo de áreas de comisiones.

## Uso

Las comisiones ahora pueden clasificarse en tres áreas:

1. **Atención Especializada** - Comisiones enfocadas solo en atención especializada
2. **Atención Primaria** - Comisiones enfocadas solo en atención primaria
3. **Mixta** - Comisiones que abarcan ambas áreas

## Aplicación de la Migración

### PostgreSQL / pgAdmin

```bash
psql -U usuario -d bd_comisiones -f database/migrations/003_agregar_area_mixta.sql
```

O desde pgAdmin:
1. Abrir Query Tool
2. Ejecutar el contenido de `003_agregar_area_mixta.sql`

### Verificación

```sql
-- Ver todos los valores del enum area_type
SELECT enumlabel 
FROM pg_enum 
WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'area_type')
ORDER BY enumlabel;
```

Resultado esperado:
```
ATENCION_ESPECIALIZADA
ATENCION_PRIMARIA
MIXTA
```

## Rollback

⚠️ **IMPORTANTE:** Eliminar valores de un ENUM en PostgreSQL es complejo y no recomendado en producción.

Solo ejecutar rollback si:
- No hay comisiones con área MIXTA
- Estás en ambiente de desarrollo/testing

```bash
psql -U usuario -d bd_comisiones -f database/migrations/003_rollback.sql
```

## Ejemplo de Uso

```java
// Crear comisión con área MIXTA
Comision comision = new Comision();
comision.setNombre("Comisión de Calidad Integral");
comision.setArea(Comision.Area.MIXTA);
comision.setTipo(Comision.Tipo.COMISION);
// ...
```

## Consultas SQL Útiles

```sql
-- Contar comisiones por área
SELECT area, COUNT(*) as total
FROM comisiones
GROUP BY area
ORDER BY area;

-- Ver todas las comisiones MIXTA
SELECT id, nombre, fecha_constitucion
FROM comisiones
WHERE area = 'MIXTA'::area_type
ORDER BY nombre;
```

## Notas Técnicas

- El enum `area_type` se modifica con `ALTER TYPE ... ADD VALUE`
- Los nuevos valores se agregan al final del enum
- No afecta a comisiones existentes
- Backward compatible: todas las comisiones actuales mantienen sus áreas
