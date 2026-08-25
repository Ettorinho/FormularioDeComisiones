-- ========================================
-- ROLLBACK 003: Eliminar área MIXTA
-- ========================================

-- ADVERTENCIA: En PostgreSQL NO se puede eliminar fácilmente un valor de un ENUM
-- Esta operación solo funciona si NO hay datos usando 'MIXTA'
-- Si count_mixta > 0:
--   1) recategorice o elimine esas comisiones manualmente;
--   2) cree backup;
--   3) ejecute una migración manual para recrear area_type sin MIXTA.

-- Verificar que no hay comisiones con área MIXTA
DO $$
DECLARE
    count_mixta INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_mixta
    FROM comisiones
    WHERE area = 'MIXTA'::area_type;
    
    IF count_mixta > 0 THEN
        RAISE EXCEPTION '❌ No se puede eliminar MIXTA: hay % comisiones usándola. Reasigne esas filas antes de recrear area_type sin MIXTA.', count_mixta;
    END IF;
    
    RAISE NOTICE '⚠️ Para continuar debe recrear manualmente area_type sin MIXTA y volver a enlazar las columnas afectadas.';
    RAISE NOTICE '⚠️ No ejecute esta operación directamente en producción sin ventana de mantenimiento y backup validado.';
    RAISE NOTICE '⚠️ Paso sugerido: preparar primero un script manual de recreación del ENUM y probarlo en staging.';
END $$;
