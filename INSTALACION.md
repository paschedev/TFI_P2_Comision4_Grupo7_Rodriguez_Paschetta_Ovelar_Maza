# Guía de Instalación - Sistema de Gestión de Pedidos y Envíos

## Requisitos Previos

### 1. Java Development Kit (JDK)
- Java 8 o superior
- Verificar instalación: `java -version`

### 2. MySQL Server
- MySQL 8.0 o superior
- Verificar instalación: `mysql --version`

### 3. MySQL Connector/J
- Descargar desde: https://dev.mysql.com/downloads/connector/j/
- Versión recomendada: 8.0.33 o superior

## Pasos de Instalación

### 1. Configurar la Base de Datos

1. Iniciar MySQL Server:
```bash
sudo systemctl start mysql
# o
sudo service mysql start
```

2. Conectarse a MySQL como root:
```bash
mysql -u root -p
```

3. Crear la base de datos:
```sql
CREATE DATABASE tfi_pedidos;
```

4. Ejecutar el script de creación de tablas:
```bash
mysql -u root -p tfi_pedidos < definicion-tablas.sql
```

### 2. Configurar el Driver MySQL

1. Descargar MySQL Connector/J desde:
   https://dev.mysql.com/downloads/connector/j/

2. Extraer el archivo JAR y copiarlo a la carpeta `lib/`:
```bash
cp mysql-connector-java-8.0.33.jar lib/
```

### 3. Configurar las Credenciales

Editar el archivo `src/database.properties` con sus credenciales de MySQL:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/tfi_pedidos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=tu_usuario_mysql
db.password=tu_contraseña_mysql
```

### 4. Compilar y Ejecutar

1. Compilar el proyecto:
```bash
./compile.sh
```

2. Ejecutar el programa:
```bash
./run.sh
```

## Verificación de la Instalación

### 1. Verificar Conexión a la Base de Datos
El programa verificará automáticamente la conexión al iniciar. Si hay problemas:

1. Verificar que MySQL esté ejecutándose
2. Verificar que la base de datos `tfi_pedidos` exista
3. Verificar las credenciales en `database.properties`
4. Verificar que el driver esté en `lib/mysql-connector-java-8.0.33.jar`

### 2. Probar Funcionalidades
1. Crear un pedido de prueba
2. Crear un envío de prueba
3. Asociar el envío al pedido
4. Buscar pedidos por cliente
5. Buscar envíos por tracking

## Solución de Problemas

### Error: "No se pudo conectar a la base de datos"
- Verificar que MySQL esté ejecutándose
- Verificar las credenciales en `database.properties`
- Verificar que la base de datos `tfi_pedidos` exista

### Error: "Driver de base de datos no encontrado"
- Verificar que `mysql-connector-java-8.0.33.jar` esté en la carpeta `lib/`
- Verificar que el nombre del archivo sea exacto

### Error: "No se pudo encontrar el archivo database.properties"
- Verificar que el archivo esté en `src/database.properties`
- Verificar que el archivo tenga permisos de lectura

### Error de compilación
- Verificar que Java JDK esté instalado
- Verificar que el driver MySQL esté en el classpath
- Ejecutar: `javac -version` para verificar la instalación

## Estructura Final del Proyecto

```
TFI_Programacion2/
├── src/
│   ├── database.properties
│   └── tfi/
│       ├── config/
│       ├── entities/
│       ├── dao/
│       ├── service/
│       └── main/
├── lib/
│   └── mysql-connector-java-8.0.33.jar
├── classes/ (generado al compilar)
├── definicion-tablas.sql
├── compile.sh
├── run.sh
├── README.md
└── INSTALACION.md
```

## Comandos Útiles

### Compilar manualmente:
```bash
javac -cp ".:lib/mysql-connector-java-8.0.33.jar" -d classes src/tfi/**/*.java
```

### Ejecutar manualmente:
```bash
java -cp ".:lib/mysql-connector-java-8.0.33.jar:classes" tfi.main.Main
```

### Verificar tablas en MySQL:
```sql
USE tfi_pedidos;
SHOW TABLES;
DESCRIBE pedidos;
DESCRIBE envios;
```
