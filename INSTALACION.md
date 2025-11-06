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
- Ya incluido en el proyecto en `lib/mysql-connector-j-8.2.0.jar`


## Pasos de Instalación

### 1. Configurar la Base de Datos

#### Configuración manual

1. Si MySQL está en Docker, verifica que el contenedor esté corriendo:
```bash
sudo docker ps | grep mysql
```

2. Si MySQL está local, asegurate que el servicio esta corriendo correctamente:
```bash
sudo systemctl status mysql
```

Si no está puedes iniciarlo con el comando:
```bash
sudo systemctl start mysql
```

3. Ejecutar el script SQL manualmente:
```bash
mysql -h localhost -P 3306 -u root -p < setup-database.sql
```

### 2. Configurar el Driver MySQL
Este paso solo es necesario si quiere una version más nueva. Ya hay un driver funcional en la carpeta `lib/`.

1. Para una version más reciente descargar MySQL Connector/J desde:
   https://dev.mysql.com/downloads/connector/j/

2. Extraer el archivo JAR y copiarlo a la carpeta `lib/`:
```bash
cp mysql-connector-java-[version].jar lib/
```

### 3. Configurar las Credenciales

Editar el archivo `src/database.properties` con sus credenciales de MySQL:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/gestion_envios?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=tu_contraseña_mysql
```

**Nota:** Si usas MySQL en un docker, cambia el puerto de `3306` al puerto utilizado por el contenedor en la URL.

### 4. Poblar la base de datos con datos de prueba (Opcional)
```bash
# Compilar primero
./compile.sh

# Ejecutar el seeder Java
java -cp ".:classes:lib/mysql-connector-j-8.2.0.jar" tfi.main.DatabaseSeeder
```

### 5. Compilar y Ejecutar

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
El programa verificará automáticamente la conexión al iniciar. 

### 2. Probar Funcionalidades
1. Crear un pedido de prueba
2. Crear un envío de prueba
3. Asociar el envío al pedido
4. Buscar pedidos por cliente
5. Buscar envíos por tracking

## Solución de Problemas

### Error: "No se pudo conectar a la base de datos"
- Verificar que MySQL esté ejecutándose:
  - Docker: `docker ps | grep mysql`
  - Local: `sudo systemctl status mysql`
- Verificar el puerto correcto (3306 para local)
- Verificar las credenciales en `src/database.properties`
- Verificar que la base de datos `gestion_envios` exista:
```bash
   mysql -h localhost -P 13306 -u root -p -e "SHOW DATABASES;" | grep gestion_envios
   ```

### Error: "Driver de base de datos no encontrado"
- Verificar que `mysql-connector-java-8.2.0.jar` esté en la carpeta `lib/`
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
│   └── mysql-connector-j-8.2.0.jar
├── classes/ (generado al compilar)
├── setup-database.sql       # Script SQL para crear BD y tablas
├── compile.sh
├── run.sh
├── README.md
└── INSTALACION.md
```

## Comandos Útiles

### Compilar manualmente:
```bash
javac -cp ".:lib/mysql-connector-java-8.2.0.jar" -d classes src/tfi/**/*.java
```

### Ejecutar manualmente:
```bash
java -cp ".:lib/mysql-connector-java-8.2.0.jar:classes" tfi.main.Main
```

### Verificar tablas en MySQL:
```sql
USE gestion_envios;
SHOW TABLES;
DESCRIBE pedidos;
DESCRIBE envios;
```
