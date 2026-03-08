-- Crear base de datos (ejecutar desde pgAdmin o psql)
CREATE DATABASE proyecto_conectores;
-- En PostgreSQL no existe el comando USE; seleccione la base de datos "proyecto_conectores"
-- en pgAdmin4 antes de ejecutar el resto del script.


-- Tabla de Estudiantes
CREATE TABLE estudiantes (
  id VARCHAR(36) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  edad INT NOT NULL
);

-- Tabla de Socios (Biblioteca)
CREATE TABLE socios (
  id VARCHAR(36) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  telefono VARCHAR(20),
  fecha_registro DATE
);

-- Tabla de Libros
CREATE TABLE libros (
  id VARCHAR(36) PRIMARY KEY,
  titulo VARCHAR(200) NOT NULL,
  autor VARCHAR(100) NOT NULL,
  isbn VARCHAR(20),
  disponible BOOLEAN DEFAULT TRUE
);

-- Tabla de Alquileres
CREATE TABLE alquileres (
  id VARCHAR(36) PRIMARY KEY,
  socio_id VARCHAR(36),
  libro_id VARCHAR(36),
  fecha_alquiler DATE,
  fecha_devolucion DATE,
  estado VARCHAR(20),
  FOREIGN KEY (socio_id) REFERENCES socios(id),
  FOREIGN KEY (libro_id) REFERENCES libros(id)
);

-- Tabla de Empleados
CREATE TABLE empleados (
  id VARCHAR(36) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  apellido VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  puesto VARCHAR(100),
  salario DECIMAL(10,2),
  fecha_contratacion DATE
);