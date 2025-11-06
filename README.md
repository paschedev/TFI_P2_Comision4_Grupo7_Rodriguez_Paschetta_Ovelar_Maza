# Sistema de Gestión de Pedidos y Envíos

Este proyecto implementa un sistema de gestión de pedidos y envíos utilizando Java con JDBC y MySQL, siguiendo el patrón DAO y la arquitectura de capas.

## Arquitectura del Proyecto

### Estructura de Carpetas
```
src/tfi/
├── config/          # Configuración de la base de datos
├── entities/        # Entidades de dominio (Pedido, Envio)
├── dao/            # Capa de acceso a datos (DAOs)
├── service/        # Capa de servicios (lógica de negocio)
└── main/           # Punto de entrada y menú de la aplicación
```

### Patrones Implementados
- **DAO (Data Access Object)**: Interfaz genérica y DAOs concretos
- **Service Layer**: Lógica de negocio y validaciones
- **Singleton**: Para la conexión a la base de datos
- **Transacciones**: Manejo de transacciones en la capa de servicios

## Requisitos del Sistema

### Base de Datos
- MySQL 8.0 o superior
- Driver MySQL Connector/J
- Base de datos: `gestion_envios`

### Configuración
Seguir los pasos descriptos en INSTALACION.md

## Características Implementadas

### Entidades
- **Pedido**: Con relación 1→1 unidireccional a Envio
- **Envio**: Con enums para empresa, tipo y estado
- Baja lógica implementada en ambas entidades

### Operaciones CRUD
- Crear, leer, actualizar y eliminar (lógico) para ambas entidades
- Asociación de envíos a pedidos
- Búsquedas específicas:
  - Pedidos por cliente y por ID
  - Envíos por tracking y por ID

### Validaciones de Negocio
- Campos obligatorios
- Formatos de datos válidos
- Unicidad de números de pedido y tracking
- Relación 1→1 entre Pedido y Envio
- Validación de costos y totales no negativos

### Manejo de Transacciones
- Transacciones automáticas en operaciones complejas
- Rollback en caso de errores
- Gestión adecuada de conexiones

## Uso del Sistema

### Compilación
```bash
javac -cp ".:mysql-connector-java-8.2.0.jar" -d . src/tfi/**/*.java
```

### Ejecución
```bash
java -cp ".:mysql-connector-java-8.2.0.jar" tfi.main.Main
```

### Menú Principal
1. **Gestionar Pedidos**: CRUD completo de pedidos
2. **Gestionar Envíos**: CRUD completo de envíos
3. **Asociar Envío a Pedido**: Establecer relación 1→1
4. **Buscar Pedidos por Cliente**: Búsqueda por nombre
5. **Buscar Envío por Tracking**: Búsqueda por número de tracking

## Estructura de la Base de Datos

### Tabla `pedidos`
- `id`: BIGINT PRIMARY KEY AUTO_INCREMENT
- `eliminado`: BOOLEAN (baja lógica)
- `numero`: VARCHAR(20) UNIQUE NOT NULL
- `fecha`: DATE NOT NULL
- `clienteNombre`: VARCHAR(120) NOT NULL
- `total`: DOUBLE(12,2) NOT NULL CHECK (total >= 0)
- `estado`: ENUM('NUEVO', 'FACTURADO', 'ENVIADO')
- `envio`: BIGINT UNIQUE (FK a envios.id)

### Tabla `envios`
- `id`: BIGINT PRIMARY KEY AUTO_INCREMENT
- `eliminado`: BOOLEAN (baja lógica)
- `tracking`: VARCHAR(40) UNIQUE
- `empresa`: ENUM('ANDREANI', 'OCA', 'CORREO_ARG') NOT NULL
- `tipo`: ENUM('ESTANDAR', 'EXPRES')
- `costo`: DOUBLE(10,2) CHECK (costo >= 0)
- `fechaDespacho`: DATE
- `fechaEstimada`: DATE
- `estado`: ENUM('EN_PREPARACION', 'EN_TRANSITO', 'ENTREGADO')

## Manejo de Errores

El sistema incluye manejo robusto de errores:
- Validación de entrada del usuario
- Verificación de existencia de registros
- Manejo de violaciones de unicidad
- Rollback automático en transacciones fallidas
- Mensajes de error claros y descriptivos

## Dependencias

- Java 8 o superior
- MySQL Connector/J 8.2.0 o superior
- MySQL Server 8.0 o superior
