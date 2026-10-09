-- ========================================
-- V23: Nuevo tipo de comisión "Formación/Taller/Sesión" y nuevo cargo "Ponente"
-- ========================================
-- Motivo:
-- Se necesita poder crear comisiones/grupos de tipo "Formación/Taller/Sesión"
-- (además de los ya existentes COMISION, GRUPO_TRABAJO, GRUPO_MEJORA), y
-- registrar en ellas miembros con el nuevo cargo "PONENTE" (además de los
-- cargos ya existentes: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE,
-- SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE).
--
-- IMPORTANTE: PostgreSQL exige que un valor de ENUM añadido con
-- "ALTER TYPE ... ADD VALUE" esté COMMITEADO antes de poder usarse (p. ej.
-- en un CHECK constraint). Como Flyway ejecuta cada migración en una única
-- transacción, esta migración se limita EXCLUSIVAMENTE a añadir los nuevos
-- valores a los ENUMs. La actualización de los CHECK constraints que los
-- referencian se hace en la migración V24, en una transacción posterior.
--
-- NOTA: Se usa la cláusula "IF NOT EXISTS" de ALTER TYPE ... ADD VALUE
-- (disponible desde PostgreSQL 12) en lugar de bloques DO $$ ... $$, para
-- evitar problemas de compatibilidad con clientes/herramientas SQL que no
-- gestionan correctamente el "dollar quoting" de PL/pgSQL.
-- ========================================

-- 1. Añadir el nuevo valor al ENUM tipo_type (idempotente)
ALTER TYPE tipo_type ADD VALUE IF NOT EXISTS 'FORMACION_TALLER_SESION';

-- 2. Añadir el nuevo valor al ENUM cargo_type (idempotente)
ALTER TYPE cargo_type ADD VALUE IF NOT EXISTS 'PONENTE';

-- Actualizar comentarios de los tipos ENUM
COMMENT ON TYPE tipo_type IS 'Tipos de comisión: COMISION, GRUPO_TRABAJO, GRUPO_MEJORA, FORMACION_TALLER_SESION';
COMMENT ON TYPE cargo_type IS 'Tipos de cargo en comisión: REFERENTE, RESPONSABLE, PRESIDENTE, PARTICIPANTE, SECRETARIO, INVESTIGADOR_PRINCIPAL, INVESTIGADOR_COLABORADOR, FIRMANTE, PONENTE';
