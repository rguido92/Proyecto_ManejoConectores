# 📚 Gestión de Alquileres de Biblioteca — Java MVC + JDBC

Aplicación de escritorio para gestionar el alquiler de libros de una biblioteca. Implementa el patrón **MVC (Modelo-Vista-Controlador)** con acceso a datos mediante **JDBC** directo sobre **PostgreSQL**.

> 🎯 Proyecto de la asignatura **Acceso a Datos** — CFGS DAM · IES Teis 2022–2024

---

## ✨ Funcionalidades

- 👥 Consultar información completa de socios de la biblioteca
- 📖 Ver catálogo de libros disponibles (no alquilados) en tiempo real
- 🔄 Gestionar alquileres: registrar préstamo y devolución
- 📋 Consultar historial completo de alquileres (libro, socio, fechas)
- 🗓️ Filtrar libros alquilados con fecha de préstamo activa

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Patrón de diseño | MVC (Modelo-Vista-Controlador) |
| Acceso a datos | JDBC (Java Database Connectivity) |
| Base de datos | PostgreSQL |
| Interfaz de usuario | Java Swing / HTML+CSS |
| Build | Maven |

---

## 🗄️ Modelo de datos

```sql
-- Socios de la biblioteca
CREATE TABLE socios (
    socio_id   SERIAL PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    email      VARCHAR(100) UNIQUE,
    telefono   VARCHAR(20)
);

-- Catálogo de libros
CREATE TABLE libros (
    libro_id   SERIAL PRIMARY KEY,
    titulo     VARCHAR(200) NOT NULL,
    autor      VARCHAR(100),
    isbn       VARCHAR(20) UNIQUE,
    disponible BOOLEAN DEFAULT TRUE
);

-- Registro de alquileres
CREATE TABLE alquileres (
    alquiler_id    SERIAL PRIMARY KEY,
    socio_id       INT REFERENCES socios(socio_id),
    libro_id       INT REFERENCES libros(libro_id),
    fecha_alquiler DATE NOT NULL,
    fecha_devolucion DATE
);
```

---

## 📁 Estructura del proyecto

```
Proyecto_ManejoConectores/
├── src/
│   ├── model/
│   │   ├── Socio.java
│   │   ├── Libro.java
│   │   └── Alquiler.java
│   ├── dao/
│   │   ├── SocioDao.java          ← CRUD socios via JDBC
│   │   ├── LibroDao.java          ← CRUD libros via JDBC
│   │   └── AlquilerDao.java       ← Gestión alquileres + historial
│   ├── controller/
│   │   └── BibliotecaController.java
│   ├── view/
│   │   └── MainView.java
│   └── util/
│       └── ConexionDB.java        ← Pool de conexiones JDBC
├── script.sql                     ← Script DDL + datos de prueba
└── README.md
```

---

## 🚀 Cómo ejecutar

### Requisitos
- Java 17+
- PostgreSQL 14+
- Maven

```bash
# 1. Clonar el repositorio
git clone https://github.com/rguido92/Proyecto_ManejoConectores.git
cd Proyecto_ManejoConectores

# 2. Crear la base de datos e importar el esquema
psql -U postgres -c "CREATE DATABASE biblioteca_db;"
psql -U postgres -d biblioteca_db -f script.sql

# 3. Configurar la conexión en ConexionDB.java
# URL: jdbc:postgresql://localhost:5432/biblioteca_db
# User: tu_usuario / Password: tu_password

# 4. Compilar y ejecutar
mvn compile
mvn exec:java -Dexec.mainClass="Main"
```

---

## 🧪 Tests

```bash
mvn test
```

---

## 💡 Conceptos aplicados

- **Patrón MVC**: separación clara entre datos, lógica y presentación
- **JDBC**: manejo de `Connection`, `PreparedStatement` y `ResultSet`
- **Gestión de transacciones**: commit/rollback en operaciones críticas
- **Patrón DAO**: abstracción del acceso a base de datos por entidad
- **SQL avanzado**: JOINs, subconsultas, filtros por fecha

---

## 👤 Autor

**Rodrigo Guido** — Desarrollador Backend Java  
[LinkedIn](https://www.linkedin.com/in/rodrigo-guido92) · [GitHub](https://github.com/rguido92) · rodrigoguidoarias@gmail.com
