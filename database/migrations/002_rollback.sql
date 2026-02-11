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
-- ADVERTENCIA: Esto eliminará todos los registros históricos

DROP TABLE IF EXISTS comision_miembro_historial_cargos CASCADE;

-- ========================================
-- VERIFICACIÓN
-- ========================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'comision_miembro_historial_cargos') THEN
        RAISE NOTICE '✅ Tabla comision_miembro_historial_cargos eliminada correctamente';
    ELSE
        RAISE WARNING '⚠️ La tabla comision_miembro_historial_cargos aún existe';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_cambio_cargo') THEN
        RAISE NOTICE '✅ Trigger trigger_cambio_cargo eliminado correctamente';
    ELSE
        RAISE WARNING '⚠️ El trigger trigger_cambio_cargo aún existe';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'registrar_cambio_cargo') THEN
        RAISE NOTICE '✅ Función registrar_cambio_cargo eliminada correctamente';
    ELSE
        RAISE WARNING '⚠️ La función registrar_cambio_cargo aún existe';
    END IF;
    
    RAISE NOTICE '✅ Rollback 002 completado';
END $$;
