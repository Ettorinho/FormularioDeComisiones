-- Migración 007: Añadir campo titulo a actas
-- Fecha: 2026-07-20
-- Autor: Sistema
-- Descripción: Añade campo titulo (VARCHAR 200) a la tabla actas

-- Añadir columna titulo
ALTER TABLE actas 
ADD COLUMN titulo VARCHAR(200);

-- Crear índice para búsquedas
CREATE INDEX idx_actas_titulo ON actas(titulo);

-- Actualizar actas existentes con un título por defecto basado en la fecha
UPDATE actas 
SET titulo = 'Reunión del ' || TO_CHAR(fecha_reunion, 'DD/MM/YYYY')
WHERE titulo IS NULL;

-- Comentario en la tabla
COMMENT ON COLUMN actas.titulo IS 'Título descriptivo del acta de reunión';
