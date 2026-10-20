# Shipment-Service

Microservicio transaccional de **LazyHawk** encargado del ciclo de vida de los envíos, la gestión de repartidores y la asignación de paquetes.

## Descripción

LazyHawk es una plataforma de logística integral orientada a comercios electrónicos y entregas de última milla.  
Este servicio constituye el núcleo de negocio del sistema: registra envíos, calcula tarifas, controla los estados del paquete, mantiene un historial de auditoría y permite la asignación de repartidores.

El microservicio está construido bajo principios de Clean Code, con persistencia relacional, API REST documentada y manejo centralizado de errores.

## Alcance funcional
- UML modificado 
<img width="965" height="569" alt="image" src="https://github.com/user-attachments/assets/21a0c8a6-5473-4ec5-aa35-916d4c16a73b" />

### Gestión de envíos
- Registrar un nuevo envío (cliente, dirección de origen, dirección de destino, peso y dimensiones)
- Consultar envíos de forma individual (por ID o por código de seguimiento)
- Consultar envíos de forma grupal filtrando por estado
- Cancelar un envío únicamente cuando se encuentra en estado `PENDIENTE`

### Actualización de estado y auditoría
- Cambiar el estado de un envío respetando el flujo permitido:
    PENDIENTE → EN_RUTA → ENTREGADO
    PENDIENTE → CANCELADO
- Registrar automáticamente un historial de auditoría con cada cambio de estado (trazabilidad + fecha/hora)

### Gestión de repartidores
- Registrar nuevos repartidores
- Consultar listado y detalle de repartidores
- Asignar un repartidor activo a un envío antes de que pase a estado `EN_RUTA`

## Stack tecnológico

| Tecnología              | Uso                                    |
|-------------------------|----------------------------------------|
| Java 25                 | Lenguaje base                          |
| Spring Boot 3.x         | Framework principal                    |
| Spring Data JPA         | Persistencia                           |
| MySQL                   | Base de datos relacional               |
| springdoc-openapi       | Documentación OpenAPI 3 / Swagger      |
| JUnit 5 + Mockito       | Pruebas unitarias                      |
| Docker + Docker Compose | Empaquetado e infraestructura          |

## Arquitectura

El proyecto sigue una arquitectura en capas claramente separadas:
```text
com.logitrack.shipment
├── controller         # Capa de exposición (API REST)
│   ├── dto            # DTOs de Request y Response (aislados del dominio)
│   └── EnvioController.java
├── service            # Lógica de negocio (Casos de uso)
│   ├── impl           # Implementaciones de los servicios
│   └── EnvioService.java
├── repository         # Capa de acceso a datos (Spring Data JPA)
│   └── EnvioRepository.java
├── domain             # Core del negocio (Puro, sin dependencias web)
│   ├── entity         # Entidades JPA (Envio, Repartidor, AuditableEntity)
│   ├── enums          # Enumeradores (EstadoEnvio, EstadoRepartidor)
│   └── vo             # Value Objects (@Embeddable Direccion)
├── exception          # Manejo global de errores (@RestControllerAdvice)
├── config             # Configuraciones (Swagger, Beans, etc.)
└── ShipmentServiceApplication.java
```
- **DTOs** para desacoplar el contrato de la API de las entidades JPA
- **UUID** como identificador de todas las entidades
- **Value Objects** para modelar conceptos del dominio (ej. Dirección)
- **Auditoría** automática en cada cambio de estado
- **Manejo global de excepciones** con `@RestControllerAdvice`
- **Validaciones de dominio** (máquina de estados y cálculo de tarifa)

## Modelo de dominio

- **Envío**: código de seguimiento, peso, dimensiones, tarifa, estado, relaciones con Cliente, Direcciones y Repartidor
- **Cliente**: datos de contacto
- **Dirección**: origen y destino (calle, número, ciudad, código postal) – modelada como Value Object
- **Repartidor**: nombre, tipo de vehículo y estado (`ACTIVO` / `INACTIVO`)
- **Estados de envío**: `PENDIENTE`, `EN_RUTA`, `ENTREGADO`, `CANCELADO`

## Requisitos previos

- Java 25
- Maven 3.9+
- Docker y Docker Compose
- MySQL 8 (solo si se ejecuta fuera de Docker)

## Ejecución con Docker

Levantar el entorno completo (aplicación + MySQL) con un solo comando:

```bash
docker-compose up --build
```
## Ejecución local

Configurar la base de datos en src/main/resources/application.yml
Ejecutar el script de inicialización de la base de datos
Arrancar la aplicación:
```bash
./mvnw spring-boot:run
```
## Testing
Pruebas unitarias

Capa de servicios con JUnit 5 y Mockito
Cobertura sobre el cálculo de tarifa y las validaciones de la máquina de estados
```bash
./mvnw test
```
## Pruebas de integración
Repositorios con @DataJpaTest
Endpoints con @WebMvcTest / @SpringBootTest
```text
## Estructura del proyectosrc/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── logitrack/
│   │           └── shipmentservice/
│   │               ├── controlador/
│   │               ├── dominio/
│   │               │   ├── dto/
│   │               │   ├── entidad/
│   │               │   ├── enumerador/
│   │               │   ├── evento/
│   │               │   ├── exception/
│   │               │   └── valueObject/
│   │               ├── repositorio/
│   │               ├── servicio/
│   │               └── ShipmentServiceApplication.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/
            └── logitrack/
                └── shipmentservice/
```
## Buenas prácticas aplicadas
Clean Code
Principios REST (recursos en plural, verbos HTTP correctos)
DTOs para desacoplamiento
Value Objects para el dominio
Manejo global de excepciones (@RestControllerAdvice)
UUID en todas las entidades
Auditoría de cambios de estado
Dockerfile multi-stage optimizado
docker-compose para entorno reproducible

## Autores
* Mamani, Luis Carlos Manuel - https://github.com/LuisMPlus
* Maygua Altamirano, Pio Roman - https://github.com/95670446-maker
* Mamani Leandro - https://github.com/leoM2022
  
Desarrollado como parte del Sprint 1 – Core de Negocio y Dominio Relacional
LazyHawk · Desarrollo y Arquitecturas Avanzadas de Software
