package server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import biblioteca.Alquiler;
import biblioteca.Empleado;
import biblioteca.Libro;
import biblioteca.ManageAlquileres;
import biblioteca.ManageEmpleados;
import biblioteca.ManageLibros;
import biblioteca.ManageSocios;
import biblioteca.Socio;
import ejercicio307.ManageStudents;
import ejercicio307.Student;


/**
 * Minimal HTTP server that serves static files from `web/` and provides a JSON API
 * for students, socios, libros, alquileres and empleados management.
 */
public class WebServer {
    private static ManageStudents manager = new ManageStudents();
    private static ManageSocios manageSocios = new ManageSocios();
    private static ManageLibros manageLibros = new ManageLibros();
    private static ManageAlquileres manageAlquileres = new ManageAlquileres();
    private static ManageEmpleados manageEmpleados = new ManageEmpleados();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // DB config: prefer environment variables, fallback to config/server.properties
        String bd = System.getenv("DB_NAME");
        String host = System.getenv("DB_HOST");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");

        if (bd == null || host == null || user == null || password == null) {
            try (InputStream in = Files.newInputStream(Paths.get("config/server.properties"))) {
                Properties p = new Properties();
                p.load(in);
                if (bd == null) bd = p.getProperty("db.name");
                if (host == null) host = p.getProperty("db.host");
                if (user == null) user = p.getProperty("db.user");
                if (password == null) password = p.getProperty("db.password");
            } catch (Exception e) {
                System.out.println("Warning: could not read config/server.properties, relying on env vars or defaults");
            }
        }

        // final defaults if still null
        if (bd == null) bd = "proyecto_conectores";
        if (host == null) host = "localhost";
        if (user == null) user = "rguido";
        if (password == null) password = "abc123";

        // Try to connect to database for all managers
        try {
            manager.openConnection(bd, host, user, password);
            manageSocios.openConnection(bd, host, user, password);
            manageLibros.openConnection(bd, host, user, password);
            manageAlquileres.openConnection(bd, host, user, password);
            manageEmpleados.openConnection(bd, host, user, password);
            System.out.println("✅ Todos los módulos conectados a la BD");
        } catch (Exception e) {
            System.out.println("⚠️ Usando almacenamiento en memoria para los módulos");
        }

        HttpServer http = HttpServer.create(new InetSocketAddress(8000), 0);
        
        // Students endpoints
        http.createContext("/api/students", new StudentsHandler());
        http.createContext("/api/students/", new StudentByIdHandler());
        
        // Socios endpoints
        http.createContext("/api/socios", new SociosHandler());
        http.createContext("/api/socios/", new SocioByIdHandler());
        
        // Libros endpoints
        http.createContext("/api/libros", new LibrosHandler());
        http.createContext("/api/libros/", new LibroByIdHandler());
        http.createContext("/api/libros/disponibles", new LibrosDisponiblesHandler());
        http.createContext("/api/libros/alquilados", new LibrosAlquiladosHandler());
        
        // Alquileres endpoints
        http.createContext("/api/alquileres", new AlquileresHandler());
        http.createContext("/api/alquileres/", new AlquilerByIdHandler());
        http.createContext("/api/alquileres/activos", new AlquileresActivosHandler());
        http.createContext("/api/alquileres/historial", new AlquileresHistorialHandler());
        
        // Empleados endpoints
        http.createContext("/api/empleados", new EmpleadosHandler());
        http.createContext("/api/empleados/", new EmpleadoByIdHandler());
        
        // Static files
        http.createContext("/", new StaticHandler());
        http.setExecutor(null);
        System.out.println("Server started at http://localhost:8000");
        http.start();
    }

    static class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Student> list = manager.getStudents();
                    String json = MAPPER.writeValueAsString(list);
                    writeJson(exchange, 200, json);
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = readBody(exchange);
                    Student s = MAPPER.readValue(body, Student.class);
                    boolean ok = manager.addStudent(s);
                    if (ok) writeJson(exchange, 201, MAPPER.writeValueAsString(new ApiStatus("created")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class StudentByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                URI uri = exchange.getRequestURI();
                String path = uri.getPath();
                // expected /api/students/{id}
                String[] parts = path.split("/");
                if (parts.length < 4) { exchange.sendResponseHeaders(400, -1); return; }
                String id = parts[3];
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    Student s = manager.getStudent(id);
                    if (s != null) writeJson(exchange, 200, MAPPER.writeValueAsString(s));
                    else exchange.sendResponseHeaders(404, -1);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    boolean ok = manager.deleteStudent(id);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("deleted")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else if (method.equalsIgnoreCase("PUT")) {
                    String body = readBody(exchange);
                    Student s = MAPPER.readValue(body, Student.class);
                    boolean ok = manager.modifyStudent(id, s);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("updated")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File f = new File("web" + path);
            if (!f.exists() || f.isDirectory()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String contentType = guessContentType(f.getName());
            byte[] bytes = Files.readAllBytes(f.toPath());
            exchange.getResponseHeaders().add("Content-Type", contentType + ";charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // ============= SOCIOS HANDLERS =============
    static class SociosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Socio> list = manageSocios.getAllSocios();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = readBody(exchange);
                    Socio s = MAPPER.readValue(body, Socio.class);
                    if (s.getId() == null || s.getId().isEmpty()) {
                        s.setId(UUID.randomUUID().toString());
                    }
                    boolean ok = manageSocios.addSocio(s);
                    if (ok) writeJson(exchange, 201, MAPPER.writeValueAsString(new ApiStatus("created")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class SocioByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length < 4) { exchange.sendResponseHeaders(400, -1); return; }
                String id = parts[3];
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    Socio s = manageSocios.getSocioById(id);
                    if (s != null) writeJson(exchange, 200, MAPPER.writeValueAsString(s));
                    else exchange.sendResponseHeaders(404, -1);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    boolean ok = manageSocios.deleteSocio(id);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("deleted")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else if (method.equalsIgnoreCase("PUT")) {
                    String body = readBody(exchange);
                    Socio s = MAPPER.readValue(body, Socio.class);
                    boolean ok = manageSocios.updateSocio(id, s);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("updated")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    // ============= LIBROS HANDLERS =============
    static class LibrosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Libro> list = manageLibros.getAllLibros();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = readBody(exchange);
                    Libro l = MAPPER.readValue(body, Libro.class);
                    if (l.getId() == null || l.getId().isEmpty()) {
                        l.setId(UUID.randomUUID().toString());
                    }
                    boolean ok = manageLibros.addLibro(l);
                    if (ok) writeJson(exchange, 201, MAPPER.writeValueAsString(new ApiStatus("created")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class LibrosDisponiblesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Libro> list = manageLibros.getLibrosDisponibles();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class LibrosAlquiladosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Libro> list = manageLibros.getLibrosAlquilados();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class LibroByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length < 4) { exchange.sendResponseHeaders(400, -1); return; }
                String id = parts[3];
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    Libro l = manageLibros.getLibroById(id);
                    if (l != null) writeJson(exchange, 200, MAPPER.writeValueAsString(l));
                    else exchange.sendResponseHeaders(404, -1);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    boolean ok = manageLibros.deleteLibro(id);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("deleted")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else if (method.equalsIgnoreCase("PUT")) {
                    String body = readBody(exchange);
                    Libro l = MAPPER.readValue(body, Libro.class);
                    boolean ok = manageLibros.updateLibro(id, l);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("updated")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    // ============= ALQUILERES HANDLERS =============
    static class AlquileresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Alquiler> list = manageAlquileres.getAllAlquileres();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = readBody(exchange);
                    Alquiler a = MAPPER.readValue(body, Alquiler.class);
                    if (a.getId() == null || a.getId().isEmpty()) {
                        a.setId(UUID.randomUUID().toString());
                    }
                    boolean ok = manageAlquileres.registrarAlquiler(a);
                    if (ok) writeJson(exchange, 201, MAPPER.writeValueAsString(new ApiStatus("created")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class AlquileresActivosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Alquiler> list = manageAlquileres.getAlquileresActivos();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class AlquileresHistorialHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Alquiler> list = manageAlquileres.getHistorialAlquileres();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class AlquilerByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length < 4) { exchange.sendResponseHeaders(400, -1); return; }
                String id = parts[3];
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    Alquiler a = manageAlquileres.getAlquilerById(id);
                    if (a != null) writeJson(exchange, 200, MAPPER.writeValueAsString(a));
                    else exchange.sendResponseHeaders(404, -1);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    boolean ok = manageAlquileres.deleteAlquiler(id);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("deleted")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else if (method.equalsIgnoreCase("PUT")) {
                    String body = readBody(exchange);
                    String accion = MAPPER.readTree(body).get("accion").asText();
                    if ("devolver".equals(accion)) {
                        boolean ok = manageAlquileres.devolverLibro(id);
                        if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("returned")));
                        else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                    } else {
                        exchange.sendResponseHeaders(400, -1);
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    // ============= EMPLEADOS HANDLERS =============
    static class EmpleadosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    ArrayList<Empleado> list = manageEmpleados.getAllEmpleados();
                    writeJson(exchange, 200, MAPPER.writeValueAsString(list));
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = readBody(exchange);
                    Empleado e = MAPPER.readValue(body, Empleado.class);
                    if (e.getId() == null || e.getId().isEmpty()) {
                        e.setId(UUID.randomUUID().toString());
                    }
                    boolean ok = manageEmpleados.addEmpleado(e);
                    if (ok) writeJson(exchange, 201, MAPPER.writeValueAsString(new ApiStatus("created")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    static class EmpleadoByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length < 4) { exchange.sendResponseHeaders(400, -1); return; }
                String id = parts[3];
                String method = exchange.getRequestMethod();
                if (method.equalsIgnoreCase("GET")) {
                    Empleado e = manageEmpleados.getEmpleadoById(id);
                    if (e != null) writeJson(exchange, 200, MAPPER.writeValueAsString(e));
                    else exchange.sendResponseHeaders(404, -1);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    boolean ok = manageEmpleados.deleteEmpleado(id);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("deleted")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else if (method.equalsIgnoreCase("PUT")) {
                    String body = readBody(exchange);
                    Empleado e = MAPPER.readValue(body, Empleado.class);
                    boolean ok = manageEmpleados.updateEmpleado(id, e);
                    if (ok) writeJson(exchange, 200, MAPPER.writeValueAsString(new ApiStatus("updated")));
                    else writeJson(exchange, 500, MAPPER.writeValueAsString(new ApiStatus("error")));
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeJson(exchange, 500, "{\"error\":\"server error\"}");
            }
        }
    }

    private static String guessContentType(String name) {
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".css")) return "text/css";
        return "application/octet-stream";
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static class ApiStatus {
        public String status;
        public ApiStatus(String status) { this.status = status; }
    }
}
