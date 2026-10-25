-- Script de inicialización de datos para repartidores (UUID almacenados como BINARY(16))
INSERT IGNORE INTO repartidores (id, nombre, dni, telefono, tipo_vehiculo, estado, fecha_creacion, fecha_actualizacion, fecha_eliminacion)
VALUES 
    (UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), 'Carlos Gómez', '30111222', '+541144332211', 'Moto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), 'Mariana López', '31222333', '+541155443322', 'Camioneta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('33333333-3333-3333-3333-333333333333'), 'Juan Pérez', '32333444', '+541166554433', 'Bicicleta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'Lucía Fernández', '33444555', '+541177665544', 'Auto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'Roberto Díaz', '34555666', '+541188776655', 'Utilitario', 'INACTIVO', NOW(), NOW(), NULL);

-- Script de inicialización de envíos para pruebas
INSERT IGNORE INTO envios (id, codigo_seguimiento, peso, ancho, alto, largo, tarifa, estado, origen_calle, origen_numero, origen_ciudad, origen_cp, origen_provincia, origen_localidad, origen_descripcion, destino_calle, destino_numero, destino_ciudad, destino_cp, destino_provincia, destino_localidad, destino_descripcion, repartidor_id, fecha_creacion, fecha_actualizacion)
VALUES 
    (UUID_TO_BIN('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100001-TEST1', 2.5, 10.0, 10.0, 15.0, 25.0, 'PENDIENTE', 'Av. Corrientes', '1234', 'CABA', '1043', 'Buenos Aires', 'San Nicolás', 'Piso 2', 'Av. Santa Fe', '4321', 'CABA', '1425', 'Buenos Aires', 'Palermo', 'Puerta negra', NULL, NOW(), NOW()),
    (UUID_TO_BIN('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100002-TEST2', 5.0, 20.0, 20.0, 20.0, 45.0, 'EN_RUTA', 'Av. Rivadavia', '5555', 'CABA', '1405', 'Buenos Aires', 'Caballito', 'Timbre B', 'Av. Belgrano', '888', 'CABA', '1092', 'Buenos Aires', 'Monserrat', 'Casa', UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), NOW(), NOW());
