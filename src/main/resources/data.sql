-- Script de inicialización de datos para repartidores (UUID almacenados como BINARY(16))
INSERT IGNORE INTO repartidores (id, nombre, tipo_vehiculo, estado, fecha_creacion, fecha_actualizacion, fecha_eliminacion)
VALUES 
    (UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), 'Carlos Gómez', 'Moto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), 'Mariana López', 'Camioneta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('33333333-3333-3333-3333-333333333333'), 'Juan Pérez', 'Bicicleta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'Lucía Fernández', 'Auto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'Roberto Díaz', 'Utilitario', 'INACTIVO', NOW(), NOW(), NULL);
