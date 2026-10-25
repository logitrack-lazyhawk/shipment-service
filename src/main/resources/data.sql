-- Script de inicialización de datos para repartidores (UUID almacenados como BINARY(16))
INSERT IGNORE INTO repartidores (id, nombre, dni, telefono, tipo_vehiculo, estado, fecha_creacion, fecha_actualizacion, fecha_eliminacion)
VALUES 
    (UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), 'Carlos Gómez', '30111222', '+541144332211', 'Moto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), 'Mariana López', '31222333', '+541155443322', 'Camioneta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('33333333-3333-3333-3333-333333333333'), 'Juan Pérez', '32333444', '+541166554433', 'Bicicleta', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('44444444-4444-4444-4444-444444444444'), 'Lucía Fernández', '33444555', '+541177665544', 'Auto', 'ACTIVO', NOW(), NOW(), NULL),
    (UUID_TO_BIN('55555555-5555-5555-5555-555555555555'), 'Roberto Díaz', '34555666', '+541188776655', 'Utilitario', 'INACTIVO', NOW(), NOW(), NULL);

-- Script de inicialización de clientes (UUID almacenados como BINARY(16))
INSERT IGNORE INTO clientes (id, nombre, dni, telefono, email, fecha_creacion, fecha_actualizacion, fecha_eliminacion)
VALUES 
    (UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), 'Martín Rodríguez', '35111222', '+541122334455', 'martin.rodriguez@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c2222222-2222-2222-2222-222222222222'), 'Valeria Morales', '36222333', '+541133445566', 'valeria.morales@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c3333333-3333-3333-3333-333333333333'), 'Esteban Quito', '37333444', '+541144556677', 'esteban.quito@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c4444444-4444-4444-4444-444444444444'), 'Camila Benítez', '38444555', '+541155667788', 'camila.benitez@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c5555555-5555-5555-5555-555555555555'), 'Gonzalo Herrera', '39555666', '+541166778899', 'gonzalo.herrera@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c6666666-6666-6666-6666-666666666666'), 'Florencia Castro', '40666777', '+541177889900', 'florencia.castro@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c7777777-7777-7777-7777-777777777777'), 'Santiago Romero', '41777888', '+541188990011', 'santiago.romero@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c8888888-8888-8888-8888-888888888888'), 'Agustina Navarro', '42888999', '+541199001122', 'agustina.navarro@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('c9999999-9999-9999-9999-999999999999'), 'Lucas Maidana', '43999000', '+541100112233', 'lucas.maidana@email.com', NOW(), NOW(), NULL),
    (UUID_TO_BIN('caaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'Julieta Silva', '44000111', '+541111223344', 'julieta.silva@email.com', NOW(), NOW(), NULL);

-- Script de inicialización de envíos para pruebas
INSERT IGNORE INTO envios (id, codigo_seguimiento, peso, ancho, alto, largo, tarifa, estado, origen_calle, origen_numero, origen_ciudad, origen_cp, origen_provincia, origen_localidad, origen_descripcion, destino_calle, destino_numero, destino_ciudad, destino_cp, destino_provincia, destino_localidad, destino_descripcion, repartidor_id, cliente_id, fecha_creacion, fecha_actualizacion)
VALUES 
    (UUID_TO_BIN('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100001-TEST1', 2.5, 10.0, 10.0, 15.0, 25.0, 'PENDIENTE', 'Av. Corrientes', '1234', 'CABA', '1043', 'Buenos Aires', 'San Nicolás', 'Piso 2', 'Av. Santa Fe', '4321', 'CABA', '1425', 'Buenos Aires', 'Palermo', 'Puerta negra', NULL, UUID_TO_BIN('c1111111-1111-1111-1111-111111111111'), NOW(), NOW()),
    (UUID_TO_BIN('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100002-TEST2', 5.0, 20.0, 20.0, 20.0, 45.0, 'EN_RUTA', 'Av. Rivadavia', '5555', 'CABA', '1405', 'Buenos Aires', 'Caballito', 'Timbre B', 'Av. Belgrano', '888', 'CABA', '1092', 'Buenos Aires', 'Monserrat', 'Casa', UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), UUID_TO_BIN('c2222222-2222-2222-2222-222222222222'), NOW(), NOW()),
    (UUID_TO_BIN('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100003-TEST3', 1.2, 5.0, 5.0, 10.0, 18.0, 'ENTREGADO', 'San Martín', '450', 'Córdoba', '5000', 'Córdoba', 'Centro', 'Oficina 3', 'Colón', '1200', 'Córdoba', '5000', 'Córdoba', 'Alberdi', 'Dejar en recepción', UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), UUID_TO_BIN('c3333333-3333-3333-3333-333333333333'), NOW(), NOW()),
    (UUID_TO_BIN('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100004-TEST4', 8.0, 30.0, 25.0, 40.0, 85.0, 'PENDIENTE', 'Mitre', '234', 'Rosario', '2000', 'Santa Fe', 'Centro', 'Local comercial', 'Pellegrini', '1500', 'Rosario', '2000', 'Santa Fe', 'Abasto', 'Portón gris', NULL, UUID_TO_BIN('c4444444-4444-4444-4444-444444444444'), NOW(), NOW()),
    (UUID_TO_BIN('aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'LT-100005-TEST5', 3.0, 15.0, 15.0, 20.0, 32.5, 'CANCELADO', 'Alvear', '789', 'Mendoza', '5500', 'Mendoza', 'Godoy Cruz', 'Depto 1A', 'San Juan', '1050', 'Mendoza', '5500', 'Mendoza', 'Ciudad', 'Casa de rejas blancas', NULL, UUID_TO_BIN('c5555555-5555-5555-5555-555555555555'), NOW(), NOW());
