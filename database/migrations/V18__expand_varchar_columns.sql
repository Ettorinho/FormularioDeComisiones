-- ========================================
-- MIGRACIÓN V18: Ampliar columnas VARCHAR a 200 caracteres
-- Fecha: 2026-08-25
-- Descripción: Ajusta las columnas nombre (comisiones) y nombre_apellidos (miembros)
--              de VARCHAR(100) a VARCHAR(200) para alinearlas con las anotaciones
--              @Size(max=200) definidas en los modelos Java Comision y Miembro.
-- ========================================

ALTER TABLE comisiones
    ALTER COLUMN nombre TYPE VARCHAR(200);

ALTER TABLE miembros
    ALTER COLUMN nombre_apellidos TYPE VARCHAR(200);
