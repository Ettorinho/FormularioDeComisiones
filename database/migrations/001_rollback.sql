-- ========================================
-- SCRIPT DE ROLLBACK PARA MIGRACIÓN 001
-- Versión: 001_rollback
-- Fecha: 2026-02-05
-- Autor: Sistema de Gestión de Comisiones
-- ========================================

-- IMPORTANTE: Este script revierte todos los cambios de 001_mejoras_criticas.sql
-- Ejecutar solo si es necesario volver al estado anterior
-- Los datos existentes se preservarán

-- ========================================
-- SECCIÓN 4 ROLLBACK: REVERTIR VALIDACIONES DE INTEGRIDAD
-- ========================================

-- 4.3. ELIMINAR VALIDACIÓN DE FECHA DE REUNIÓN
ALTER TABLE actas DROP CONSTRAINT IF EXISTS check_fecha_reunion;

-- 4.2. ELIMINAR VALIDACIÓN DE EMAIL
ALTER TABLE miembros DROP CONSTRAINT IF EXISTS check_email;

-- 4.1. REVERTIR TAMAÑO DE DNI/NIF
-- ADVERTENCIA: Si hay datos con DNI/NIF > 10 caracteres, esta operación fallará
-- En ese caso, mantener VARCHAR(15) o limpiar datos primero
ALTER TABLE miembros ALTER COLUMN dni_nif TYPE VARCHAR(10);

-- ========================================
-- SECCIÓN 3 ROLLBACK: REVERTIR MEJORAS DE AUDITORÍA
-- ========================================

-- 3.3. ELIMINAR TRIGGERS
DROP TRIGGER IF EXISTS trigger_asistencias_modificacion ON asistencias_actas;
DROP TRIGGER IF EXISTS trigger_comision_miembros_modificacion ON comision_miembros;
DROP TRIGGER IF EXISTS trigger_actas_modificacion ON actas;
DROP TRIGGER IF EXISTS trigger_miembros_modificacion ON miembros;
DROP TRIGGER IF EXISTS trigger_comisiones_modificacion ON comisiones;

-- 3.2. ELIMINAR FUNCIÓN
DROP FUNCTION IF EXISTS actualizar_fecha_modificacion();

-- 3.1. ELIMINAR CAMPOS DE AUDITORÍA AÑADIDOS
-- Nota: Solo se eliminan si fueron agregados por la migración
-- Si ya existían antes, no se tocan

-- Eliminar fecha_modificacion de asistencias_actas (si se agregó en migración)
ALTER TABLE asistencias_actas DROP COLUMN IF EXISTS fecha_modificacion;

-- Eliminar campos de auditoría de comision_miembros (si se agregaron en migración)
ALTER TABLE comision_miembros DROP COLUMN IF EXISTS fecha_modificacion;
ALTER TABLE comision_miembros DROP COLUMN IF EXISTS fecha_creacion;

-- ========================================
-- SECCIÓN 2 ROLLBACK: REVERTIR MEJORAS DE ESCALABILIDAD
-- ========================================

-- 2.2. REVERTIR SECUENCIAS A INTEGER
ALTER SEQUENCE miembros_id_seq AS INTEGER;
ALTER SEQUENCE comisiones_id_seq AS INTEGER;

-- 2.1. REVERTIR TIPOS DE ID A INTEGER
-- ADVERTENCIA: Si hay IDs > 2147483647, esta operación fallará
-- En ese caso, mantener BIGINT

-- Revertir claves foráneas en asistencias_actas
ALTER TABLE asistencias_actas ALTER COLUMN miembro_id TYPE INTEGER;

-- Revertir claves foráneas en actas
ALTER TABLE actas ALTER COLUMN comision_id TYPE INTEGER;

-- Revertir claves foráneas en comision_miembros
ALTER TABLE comision_miembros ALTER COLUMN miembro_id TYPE INTEGER;
ALTER TABLE comision_miembros ALTER COLUMN comision_id TYPE INTEGER;

-- Revertir ID de miembros a INTEGER
ALTER TABLE miembros ALTER COLUMN id TYPE INTEGER;

-- Revertir ID de comisiones a INTEGER
ALTER TABLE comisiones ALTER COLUMN id TYPE INTEGER;

-- ========================================
-- SECCIÓN 1 ROLLBACK: REVERTIR CORRECCIONES CRÍTICAS
-- ========================================

-- 1.2. ELIMINAR CONSTRAINT DE CARGO
-- Nota: El constraint original no existía, así que solo lo eliminamos
ALTER TABLE comision_miembros DROP CONSTRAINT IF EXISTS check_cargo;

-- 1.1. REVERTIR NOMBRE DE TABLA
-- Renombrar secuencia
ALTER SEQUENCE IF EXISTS asistencias_actas_id_seq RENAME TO asistencias_acta_id_seq;

-- Renombrar tabla
ALTER TABLE IF EXISTS asistencias_actas RENAME TO asistencias_acta;

-- ========================================
-- VERIFICACIÓN POST-ROLLBACK
-- ========================================

-- Verificar que la tabla se revirtió correctamente
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'asistencias_acta') THEN
        RAISE NOTICE '✓ Tabla asistencias_acta restaurada correctamente';
    ELSE
        RAISE WARNING '⚠ ADVERTENCIA: Tabla asistencias_acta no encontrada';
    END IF;
END $$;

-- Verificar que los triggers fueron eliminados
DO $$
DECLARE
    trigger_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO trigger_count
    FROM information_schema.triggers 
    WHERE trigger_name LIKE 'trigger_%_modificacion';
    
    IF trigger_count = 0 THEN
        RAISE NOTICE '✓ Todos los triggers eliminados correctamente';
    ELSE
        RAISE WARNING '⚠ Aún quedan % triggers', trigger_count;
    END IF;
END $$;

-- Verificar que los constraints fueron eliminados
DO $$
DECLARE
    constraint_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO constraint_count
    FROM information_schema.check_constraints
    WHERE constraint_schema = 'public'
      AND constraint_name IN ('check_cargo', 'check_email', 'check_fecha_reunion');
    
    IF constraint_count = 0 THEN
        RAISE NOTICE '✓ Todos los constraints de validación eliminados';
    ELSE
        RAISE WARNING '⚠ Aún quedan % constraints', constraint_count;
    END IF;
END $$;

-- Fin del script de rollback
-- ========================================
-- ROLLBACK COMPLETADO
-- ========================================
