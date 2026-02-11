-- ========================================
-- ROLLBACK 003: Eliminar área MIXTA
-- ========================================

-- ADVERTENCIA: En PostgreSQL NO se puede eliminar fácilmente un valor de un ENUM
-- Esta operación solo funciona si NO hay datos usando 'MIXTA'

-- Verificar que no hay comisiones con área MIXTA
DO $$
DECLARE
    count_mixta INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_mixta
    FROM comisiones
    WHERE area = 'MIXTA'::area_type;
    
    IF count_mixta > 0 THEN
        RAISE EXCEPTION '❌ No se puede eliminar MIXTA: hay % comisiones usándola', count_mixta;
    END IF;
    
    RAISE NOTICE '⚠️ Eliminar valores de ENUM requiere recrear el tipo completo';
    RAISE NOTICE '⚠️ No es una operación recomendada en producción';
    RAISE NOTICE '⚠️ Si es necesario, considerar migración manual compleja';
END $$;
