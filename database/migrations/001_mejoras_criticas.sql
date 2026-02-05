-- ========================================
-- SCRIPT DE MEJORAS PARA BASE DE DATOS
-- Versión: 001
-- Fecha: 2026-02-05
-- Autor: Sistema de Gestión de Comisiones
-- ========================================

-- IMPORTANTE: Hacer BACKUP de la base de datos antes de ejecutar
-- Comando sugerido: pg_dump -U postgres -d nombre_bd > backup_pre_migracion.sql

-- ========================================
-- SECCIÓN 1: CORRECCIONES CRÍTICAS
-- ========================================

-- 1.1. RENOMBRAR TABLA (CRÍTICO - Inconsistencia con código Java)
-- Problema: La tabla se llama 'asistencias_acta' pero debería ser 'asistencias_actas' para consistencia
ALTER TABLE IF EXISTS asistencias_acta RENAME TO asistencias_actas;

-- Renombrar también la secuencia asociada
ALTER SEQUENCE IF EXISTS asistencias_acta_id_seq RENAME TO asistencias_actas_id_seq;

-- 1.2. ACTUALIZAR CONSTRAINT DE CARGO (CRÍTICO - Falta PARTICIPANTE)
-- Problema: El constraint check_cargo no existe pero debe incluir PARTICIPANTE
-- Nota: Si el constraint existe, se elimina primero para recrearlo
ALTER TABLE comision_miembros DROP CONSTRAINT IF EXISTS check_cargo;
ALTER TABLE comision_miembros 
ADD CONSTRAINT check_cargo 
CHECK (cargo IN ('REFERENTE', 'RESPONSABLE', 'PRESIDENTE', 'MIEMBRO', 'SECRETARIO', 'PARTICIPANTE', 'INVESTIGADOR_PRINCIPAL', 'INVESTIGADOR_COLABORADOR'));

-- ========================================
-- SECCIÓN 2: MEJORAS DE ESCALABILIDAD
-- ========================================

-- 2.1. UNIFICAR TIPOS DE ID A BIGINT
-- Problema: comisiones.id y miembros.id usan INTEGER mientras actas.id usa BIGINT
-- Solución: Estandarizar todos a BIGINT para mejor escalabilidad

-- Convertir ID de comisiones a BIGINT
ALTER TABLE comisiones ALTER COLUMN id TYPE BIGINT;

-- Convertir ID de miembros a BIGINT
ALTER TABLE miembros ALTER COLUMN id TYPE BIGINT;

-- Actualizar claves foráneas en comision_miembros
ALTER TABLE comision_miembros ALTER COLUMN comision_id TYPE BIGINT;
ALTER TABLE comision_miembros ALTER COLUMN miembro_id TYPE BIGINT;

-- Actualizar claves foráneas en actas
ALTER TABLE actas ALTER COLUMN comision_id TYPE BIGINT;

-- Actualizar claves foráneas en asistencias_actas
ALTER TABLE asistencias_actas ALTER COLUMN miembro_id TYPE BIGINT;

-- 2.2. ACTUALIZAR SECUENCIAS A BIGINT
ALTER SEQUENCE comisiones_id_seq AS BIGINT;
ALTER SEQUENCE miembros_id_seq AS BIGINT;

-- ========================================
-- SECCIÓN 3: MEJORAS DE AUDITORÍA
-- ========================================

-- 3.1. AÑADIR CAMPOS DE AUDITORÍA FALTANTES
-- Añadir campos de auditoría a comision_miembros
ALTER TABLE comision_miembros 
ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Añadir campo de modificación a asistencias_actas
ALTER TABLE asistencias_actas 
ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 3.2. FUNCIÓN PARA ACTUALIZAR fecha_modificacion AUTOMÁTICAMENTE
-- Esta función se ejecuta en cada UPDATE para mantener fecha_modificacion actualizada
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3.3. CREAR TRIGGERS PARA TODAS LAS TABLAS
-- Estos triggers actualizan automáticamente fecha_modificacion en cada UPDATE

-- Trigger para comisiones
DROP TRIGGER IF EXISTS trigger_comisiones_modificacion ON comisiones;
CREATE TRIGGER trigger_comisiones_modificacion
BEFORE UPDATE ON comisiones
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

-- Trigger para miembros
DROP TRIGGER IF EXISTS trigger_miembros_modificacion ON miembros;
CREATE TRIGGER trigger_miembros_modificacion
BEFORE UPDATE ON miembros
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

-- Trigger para actas
DROP TRIGGER IF EXISTS trigger_actas_modificacion ON actas;
CREATE TRIGGER trigger_actas_modificacion
BEFORE UPDATE ON actas
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

-- Trigger para comision_miembros
DROP TRIGGER IF EXISTS trigger_comision_miembros_modificacion ON comision_miembros;
CREATE TRIGGER trigger_comision_miembros_modificacion
BEFORE UPDATE ON comision_miembros
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

-- Trigger para asistencias_actas
DROP TRIGGER IF EXISTS trigger_asistencias_modificacion ON asistencias_actas;
CREATE TRIGGER trigger_asistencias_modificacion
BEFORE UPDATE ON asistencias_actas
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

-- ========================================
-- SECCIÓN 4: VALIDACIONES DE INTEGRIDAD
-- ========================================

-- 4.1. AMPLIAR TAMAÑO DE DNI/NIF (para NIE, pasaportes internacionales)
-- Problema: VARCHAR(10) puede ser insuficiente para algunos documentos
ALTER TABLE miembros ALTER COLUMN dni_nif TYPE VARCHAR(15);

-- 4.2. VALIDACIÓN DE EMAIL
-- Añade constraint para asegurar formato válido de email
ALTER TABLE miembros DROP CONSTRAINT IF EXISTS check_email;
ALTER TABLE miembros 
ADD CONSTRAINT check_email 
CHECK (correo_electronico ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- 4.3. VALIDACIÓN DE FECHA DE REUNIÓN (no puede ser futura)
-- Previene fechas de reunión en el futuro
ALTER TABLE actas DROP CONSTRAINT IF EXISTS check_fecha_reunion;
ALTER TABLE actas 
ADD CONSTRAINT check_fecha_reunion 
CHECK (fecha_reunion <= CURRENT_DATE);

-- ========================================
-- VERIFICACIÓN POST-MIGRACIÓN
-- ========================================

-- Verificar que la tabla se renombró correctamente
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'asistencias_actas') THEN
        RAISE NOTICE '✓ Tabla asistencias_actas renombrada correctamente';
    ELSE
        RAISE EXCEPTION '✗ ERROR: Tabla asistencias_actas no encontrada';
    END IF;
END $$;

-- Verificar triggers creados
DO $$
DECLARE
    trigger_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO trigger_count
    FROM information_schema.triggers 
    WHERE trigger_name LIKE 'trigger_%_modificacion';
    
    IF trigger_count = 5 THEN
        RAISE NOTICE '✓ Todos los triggers (%) creados correctamente', trigger_count;
    ELSE
        RAISE WARNING '⚠ Se esperaban 5 triggers pero se encontraron %', trigger_count;
    END IF;
END $$;

-- Listar triggers creados
SELECT 
    trigger_name, 
    event_object_table, 
    action_timing || ' ' || event_manipulation AS cuando
FROM information_schema.triggers 
WHERE trigger_name LIKE 'trigger_%_modificacion'
ORDER BY event_object_table;

-- Verificar constraints de validación
SELECT 
    table_name,
    constraint_name,
    check_clause
FROM information_schema.check_constraints
WHERE constraint_schema = 'public'
  AND constraint_name IN ('check_cargo', 'check_email', 'check_fecha_reunion')
ORDER BY table_name, constraint_name;

-- Fin del script
-- ========================================
-- MIGRACIÓN COMPLETADA EXITOSAMENTE
-- ========================================
