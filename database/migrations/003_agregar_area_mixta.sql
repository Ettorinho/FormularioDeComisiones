-- ========================================
-- MIGRACIÓN 003: Agregar área MIXTA
-- Fecha: 2026-02-11
-- Descripción: Agrega el área MIXTA al enum area_type para comisiones que abarcan múltiples áreas
-- ========================================

-- Agregar valor al ENUM area_type
ALTER TYPE area_type ADD VALUE IF NOT EXISTS 'MIXTA';

-- Verificación
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'MIXTA' 
        AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'area_type')
    ) THEN
        RAISE NOTICE '✅ Área MIXTA agregada correctamente';
    ELSE
        RAISE EXCEPTION '❌ Error: No se pudo agregar MIXTA al enum area_type';
    END IF;
    
    RAISE NOTICE '✅ Migración 003 completada exitosamente';
END $$;

-- Comentario
COMMENT ON TYPE area_type IS 'Tipos de área: ATENCION_ESPECIALIZADA, ATENCION_PRIMARIA, MIXTA';
