# Shipment-Service

Microservicio transaccional de **LazyHawk** encargado del ciclo de vida de los envíos, cálculo de tarifas, gestión de clientes y coordinación de asignación de repartidores mediante comunicación inter-servicio.

---

## 📌 Descripción General

LazyHawk es una plataforma de logística integral orientada a comercios electrónicos y entregas de última milla.  
Este servicio constituye el núcleo transaccional del sistema: registra clientes y paquetes, genera códigos únicos de seguimiento (E.164 / formato estándar LT-XXXXXX-XXXXX), calcula tarifas volumétricas/por peso, valida máquinas de estado y coordina la disponibilidad de repartidores de forma desacoplada con el microservicio courier-service.

---

## 📐 Alcance Funcional y Modelo UML

![Diagrama de Clases UML](./docs/img/uml_shipment_service.png)

### 📦 Gestión de Envíos
- Registro de envíos con validaciones dimensionales, cálculo de tarifa y generación de código de seguimiento único optimizado con StringBuilder.
- Consultas individuales (por ID UUID o por código de seguimiento).
- Listados paginados con filtro opcional por estado.
- Cancelación controlada de envíos en estado PENDIENTE.

### 👤 Gestión de Clientes
- Registro y actualización de clientes con sanitización automática de DNI y normalización telefónica.
- Soft delete automático basado en timestamp (echa_eliminacion).
- Aplanamiento directo de atributos en Cliente (
ombre, dni, 	elefono, email) heredando de EntidadAuditable.

### 🔄 Transiciones de Estado y Auditoría
- Flujo estricto de estados:
  - PENDIENTE → EN_RUTA → ENTREGADO
  - PENDIENTE → CANCELADO
- Registro inmutable de auditoría (HistorialEnvio) por cada transición (estado anterior, estado nuevo, fecha/hora y actor).

### 🚚 Asignación de Repartidores (Integración Desacoplada)
- La gestión y persistencia de repartidores reside en courier-service.
- shipment-service valida la existencia y disponibilidad del repartidor de forma síncrona mediante un cliente declarativo **OpenFeign** (CourierClient).
- Tolerancia a fallos con **Resilience4j Circuit Breaker** y fallback ante indisponibilidad del servicio externo.

---

## 🛠️ Stack Tecnológico

| Tecnología | Descripción / Uso |
|---|---|
| **Java 25** | Lenguaje base de desarrollo |
| **Spring Boot 4.x / 3.x** | Framework principal de microservicios |
| **Spring Data JPA & Hibernate** | Mapeo objeto-relacional y persistencia |
| **Spring Cloud OpenFeign** | Cliente HTTP declarativo para comunicación inter-servicio |
| **Spring Cloud Config Client** | Consumo de configuración centralizada desde configserver-ms |
| **Resilience4j** | Circuit Breaker y Time Limiter para llamadas remotas |
| **MySQL 8** | Base de datos relacional transaccional |
| **H2 Database** | Base de datos en memoria para pruebas automatizadas |
| **Springdoc OpenAPI (Swagger)** | Documentación interactiva de APIs REST |
| **JUnit 5 & Mockito** | Pruebas unitarias y de integración |
| **Docker & Docker Compose** | Contenerización multi-etapa e infraestructura local |

---

## 🏛️ Arquitectura del Proyecto

El microservicio implementa una arquitectura en capas desacoplada:

`	ext
src/main/java/com/logitrack/shipmentservice/
├── cliente/
│   └── courier/          # Cliente OpenFeign y DTOs remotos hacia courier-service
├── config/               # Fallback de Feign, ErrorDecoder, Swagger y beans
├── controlador/          # Endpoints REST (@RestController)
│   └── exception/        # Manejador global de excepciones (@RestControllerAdvice)
├── dominio/
│   ├── dto/              # DTOs de Request y Response (inmutables / records)
│   ├── entidad/          # Entidades JPA (Cliente, Envio, HistorialEnvio, EntidadAuditable)
│   ├── enumerador/       # Enumeraciones de dominio (EstadoEnvio)
│   ├── exception/        # Excepciones de negocio (ReglaNegocioException, etc.)
│   └── valueObject/      # Value Objects (@Embeddable Direccion)
├── repositorio/          # Interfaces Spring Data JPA
├── servicio/             # Interfaces de lógica de negocio y validadores
│   ├── impl/             # Implementaciones de servicios transaccionales
│   └── validador/        # Validadores de reglas de negocio y clientes externos
└── ShipmentServiceApplication.java
`

---

## 🌐 Configuración Centralizada (configserver-ms)

shipment-service delega su configuración al servidor centralizado Spring Cloud Config:
- **URL de importación**: optional:configserver:\
- **Perfil activo**: \

### Principales variables de entorno:
- CONFIG_SERVER_URL: URL del Config Server (ej. http://config-server:8085).
- SPRING_PROFILES_ACTIVE: Perfil de ejecución (dev, prod).
- DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD: Parámetros de conexión a MySQL.
- COURIER_SERVICE_URL: URL base de courier-service (ej. http://courier-service:8081).

---

## 🧪 Pruebas Automatizadas

Para compilar y ejecutar la suite completa de pruebas unitarias y de persistencia:

`ash
./mvnw clean test
`

---

## 👥 Equipo y Autores
* **Mamani, Luis Carlos Manuel** - [LuisMPlus](https://github.com/LuisMPlus)
* **Maygua Altamirano, Pio Roman** - [95670446-maker](https://github.com/95670446-maker)
* **Mamani Leandro** - [leoM2022](https://github.com/leoM2022)

LazyHawk · Desarrollo y Arquitecturas Avanzadas de Software