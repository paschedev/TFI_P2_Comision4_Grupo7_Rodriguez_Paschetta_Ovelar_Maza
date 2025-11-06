-- Script de configuración inicial de la base de datos
-- Este script crea la base de datos, selecciona el esquema y crea todas las tablas necesarias

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS gestion_envios;

USE gestion_envios;

-- Eliminar tablas si existen (en orden inverso por las FK)
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS envios;

-- Crear tabla de envíos
CREATE TABLE envios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    eliminado BOOLEAN DEFAULT FALSE,
    tracking VARCHAR(40) UNIQUE,
    empresa ENUM('ANDREANI', 'OCA', 'CORREO_ARG') NOT NULL,
    tipo ENUM('ESTANDAR', 'EXPRES') NOT NULL,
    costo DOUBLE(10,2) CHECK ( costo >= 0 ) NOT NULL,
    fechaDespacho DATE,
    fechaEstimada DATE,
    estado ENUM('EN_PREPARACION', 'EN_TRANSITO', 'ENTREGADO') DEFAULT 'EN_PREPARACION' NOT NULL
);

-- Crear tabla de pedidos
CREATE TABLE pedidos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    eliminado BOOLEAN DEFAULT FALSE,
    numero VARCHAR(20) UNIQUE NOT NULL,
    fecha DATE NOT NULL,
    clienteNombre VARCHAR(120) NOT NULL,
    total DOUBLE(12,2) NOT NULL CHECK ( total >= 0 ),
    estado ENUM('NUEVO', 'FACTURADO', 'ENVIADO') DEFAULT 'NUEVO' NOT NULL,
    envio BIGINT UNIQUE,
    FOREIGN KEY (envio) REFERENCES envios(id) ON DELETE CASCADE
);


