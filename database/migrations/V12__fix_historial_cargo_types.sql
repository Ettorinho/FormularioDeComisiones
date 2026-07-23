-- ========================================
-- SCRIPT DE MIGRACIÓN: Corregir tipos de cargo en historial
-- Versión: 005
-- Fecha: 2026-03-20
-- Descripción: Convierte cargo_anterior y cargo_nuevo de VARCHAR(100)
--              a cargo_type ENUM en comision_miembro_historial_cargos,
--              para mantener consistencia con comision_miembros.cargo.
-- ========================================

-- IMPORTANTE: Hacer BACKUP antes de ejecutar
-- pg_dump -U postgres -d nombre_bd > backup_pre_migracion_005.sql

-- Eliminar constraints CHECK existentes (serán redundantes tras convertir a ENUM)
ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_nuevo;

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_anterior;

ALTER TABLE comision_miembro_historial_cargos
    DROP CONSTRAINT IF EXISTS check_cargo_diferente;

-- Convertir cargo_nuevo a cargo_type ENUM
ALTER TABLE comision_miembro_historial_cargos
    ALTER COLUMN cargo_nuevo TYPE cargo_type USING cargo_nuevo::cargo_type;

-- Convertir cargo_anterior a cargo_type ENUM (nullable)
ALTER TABLE comision_miembro_historial_cargos
    ALTER COLUMN cargo_anterior TYPE cargo_type USING cargo_anterior::cargo_type;

-- Restaurar el constraint check_cargo_diferente (ahora con tipos ENUM)
ALTER TABLE comision_miembro_historial_cargos
    ADD CONSTRAINT check_cargo_diferente
    CHECK (cargo_anterior IS NULL OR cargo_anterior != cargo_nuevo);

-- Actualizar comentarios de columnas
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_nuevo    IS 'Cargo después del cambio (tipo ENUM cargo_type)';
COMMENT ON COLUMN comision_miembro_historial_cargos.cargo_anterior IS 'Cargo antes del cambio, NULL en primer registro (tipo ENUM cargo_type)';

-- Verificación
DO $$
DECLARE
    col_type_nuevo    TEXT;
    col_type_anterior TEXT;
BEGIN
    SELECT data_type INTO col_type_nuevo
    FROM information_schema.columns
    WHERE table_name = 'comision_miembro_historial_cargos'
      AND column_name = 'cargo_nuevo';

    SELECT data_type INTO col_type_anterior
    FROM information_schema.columns
    WHERE table_name = 'comision_miembro_historial_cargos'
      AND column_name = 'cargo_anterior';

    IF col_type_nuevo = 'USER-DEFINED' AND col_type_anterior = 'USER-DEFINED' THEN
        RAISE NOTICE '✅ Columnas cargo_nuevo y cargo_anterior convertidas a ENUM cargo_type correctamente';
    ELSE
        RAISE WARNING '⚠ Tipos inesperados: cargo_nuevo=%, cargo_anterior=%', col_type_nuevo, col_type_anterior;
    END IF;

    RAISE NOTICE '✅ Migración 005 completada exitosamente';
END $$;
