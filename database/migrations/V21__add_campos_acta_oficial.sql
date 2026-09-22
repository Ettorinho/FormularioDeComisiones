-- ========================================
-- MIGRACIÓN V21: Campos del acta oficial
-- Fecha: 2026-09-22
-- Descripción: Añade campos opcionales para adaptar actas y generación
--              documental al diseño oficial basado en el PDF real de referencia.
--              Todas las columnas nuevas son NULLABLE para mantener
--              retrocompatibilidad con actas históricas.
-- ========================================

ALTER TABLE actas
    ADD COLUMN hora_inicio VARCHAR(5),
    ADD COLUMN hora_fin VARCHAR(5),
    ADD COLUMN duracion VARCHAR(100),
    ADD COLUMN tipo_reunion VARCHAR(20),
    ADD COLUMN tipo_reunion_otros_detalle TEXT,
    ADD COLUMN orden_dia TEXT,
    ADD COLUMN excusa_asistencia TEXT;
