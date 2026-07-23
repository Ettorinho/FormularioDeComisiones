-- ========================================
-- SCRIPT DE ROLLBACK: HISTORIAL DE CAMBIOS DE CARGO
-- Versión: 002
-- Fecha: 2026-02-11
-- Descripción: Revierte la migración 002_historial_cargos.sql
-- ========================================

-- ADVERTENCIA: Este script eliminará PERMANENTEMENTE todos los datos del historial de cargos
-- Hacer BACKUP antes de ejecutar: pg_dump -U postgres -d nombre_bd > backup_pre_rollback_002.sql

-- ========================================
-- PASO 1: ELIMINAR TRIGGER
-- ========================================

DROP TRIGGER IF EXISTS trigger_cambio_cargo ON comision_miembros;

-- ========================================
-- PASO 2: ELIMINAR FUNCIÓN
-- ========================================

DROP FUNCTION IF EXISTS registrar_cambio_cargo();

-- ========================================
-- PASO 3: ELIMINAR TABLA
-- ========================================
-- ADVERTENCIA: Operación destructiva.
-- DROP TABLE ... CASCADE eliminará la tabla de historial y cualquier objeto dependiente.
-- Revise dependencias con:
--   SELECT objid::regclass, refobjid::regclass FROM pg_depend WHERE refobjid = 'comision_miembro_historial_cargos'::regclass;
-- y confirme que existe un backup antes de continuar.

DROP TABLE IF EXISTS comision_miembro_historial_cargos CASCADE;

-- ========================================
-- VERIFICACIÓN
-- ========================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'comision_miembro_historial_cargos') THEN
        RAISE NOTICE '✅ Tabla comision_miembro_historial_cargos eliminada correctamente';
    ELSE
        RAISE WARNING '⚠️ La tabla comision_miembro_historial_cargos aún existe. Revise bloqueos/permisos y vuelva a ejecutar DROP TABLE manualmente solo tras verificar dependencias.';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_cambio_cargo') THEN
        RAISE NOTICE '✅ Trigger trigger_cambio_cargo eliminado correctamente';
    ELSE
        RAISE WARNING '⚠️ El trigger trigger_cambio_cargo aún existe. Revise si quedó recreado en otra tabla o si faltan permisos para eliminarlo.';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'registrar_cambio_cargo') THEN
        RAISE NOTICE '✅ Función registrar_cambio_cargo eliminada correctamente';
    ELSE
        RAISE WARNING '⚠️ La función registrar_cambio_cargo aún existe. Compruebe si otro objeto depende de ella antes de forzar su eliminación.';
    END IF;
    
    RAISE NOTICE '✅ Rollback 002 completado. Verifique la aplicación: el cambio de cargo dejará de registrar historial hasta re-aplicar V6 y V10.';
END $$;
