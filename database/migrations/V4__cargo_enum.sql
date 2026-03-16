-- ========================================
-- MIGRACIÓN V4: Convertir cargo a ENUM cargo_type
-- Fecha: 2026-03-16
-- ========================================

-- Paso 1: Crear el tipo ENUM (solo si no existe)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'cargo_type') THEN
        CREATE TYPE cargo_type AS ENUM (
            'REFERENTE',
            'RESPONSABLE',
            'PRESIDENTE',
            'PARTICIPANTE',
            'SECRETARIO',
            'INVESTIGADOR_PRINCIPAL',
            'INVESTIGADOR_COLABORADOR'
        );
    END IF;
END
$$;

-- Paso 2: Normalizar datos — convertir 'MIEMBRO' legacy a 'PARTICIPANTE'
UPDATE comision_miembros SET cargo = 'PARTICIPANTE' WHERE cargo = 'MIEMBRO';

-- Paso 3: Eliminar el CHECK constraint antiguo
ALTER TABLE comision_miembros DROP CONSTRAINT IF EXISTS check_cargo;

-- Paso 4: Cambiar el tipo de la columna de VARCHAR a cargo_type ENUM
ALTER TABLE comision_miembros
    ALTER COLUMN cargo TYPE cargo_type USING cargo::cargo_type;

-- Paso 5: Comentario de documentación
COMMENT ON COLUMN comision_miembros.cargo IS 'Cargo del miembro en la comisión (tipo ENUM cargo_type)';
