package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ejercicio307.ManageStudents;
import ejercicio307.Student;

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


/**
 * Minimal HTTP server that serves static files from `web/` and provides a tiny JSON API
 * for the students exercise. No external dependencies required (uses com.sun.net.httpserver).
 */
public class WebServer {
    private static ManageStudents manager = new ManageStudents();
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
        if (bd == null) bd = "school";
        if (host == null) host = "localhost";
        if (user == null) user = "root";
        if (password == null) password = "abc123";

        manager.openConnection(bd, host, user, password);

        HttpServer http = HttpServer.create(new InetSocketAddress(8000), 0);
        http.createContext("/api/students", new StudentsHandler());
        http.createContext("/api/students/", new StudentByIdHandler());
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
}
