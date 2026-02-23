package biblioteca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageSocios {
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet resultSet;
    private boolean useDatabase = false;
    private ArrayList<Socio> socios;

    public ManageSocios() {
        socios = new ArrayList<>();
        socios.add(new Socio("S001", "Juan", "García", "juan@example.com", "123456789", "2025-01-15"));
        socios.add(new Socio("S002", "María", "López", "maria@example.com", "987654321", "2025-02-01"));
        socios.add(new Socio("S003", "Pedro", "Martínez", "pedro@example.com", "555666777", "2025-02-05"));
    }

    public void openConnection(String bd, String host, String user, String password) {
        try {
            String url = String.format("jdbc:mysql://%s:3306/%s", host, bd);
            this.connection = DriverManager.getConnection(url, user, password);
            useDatabase = true;
            System.out.println("✅ ManageSocios: Conectado a BD");
            initDB();
        } catch (SQLException e) {
            useDatabase = false;
            System.out.println("⚠️ ManageSocios: Usando almacenamiento en memoria");
        }
    }

    private void initDB() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS socios (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "apellido VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "telefono VARCHAR(20), " +
                    "fecha_registro DATE)";
            ps = connection.prepareStatement(createTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Socio> getAllSocios() {
        if (!useDatabase) {
            return new ArrayList<>(socios);
        }

        ArrayList<Socio> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM socios";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Socio s = new Socio(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("email"),
                        resultSet.getString("telefono"),
                        resultSet.getString("fecha_registro")
                );
                lista.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Socio getSocioById(String id) {
        if (!useDatabase) {
            return socios.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        try {
            String query = "SELECT * FROM socios WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return new Socio(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("email"),
                        resultSet.getString("telefono"),
                        resultSet.getString("fecha_registro")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addSocio(Socio socio) {
        if (!useDatabase) {
            if (getSocioById(socio.getId()) != null) {
                return false;
            }
            return socios.add(socio);
        }

        try {
            String query = "INSERT INTO socios (id, nombre, apellido, email, telefono, fecha_registro) VALUES (?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(query);
            ps.setString(1, socio.getId());
            ps.setString(2, socio.getNombre());
            ps.setString(3, socio.getApellido());
            ps.setString(4, socio.getEmail());
            ps.setString(5, socio.getTelefono());
            ps.setString(6, socio.getFechaRegistro());
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSocio(String id, Socio socioAct) {
        if (!useDatabase) {
            Socio socio = getSocioById(id);
            if (socio == null) return false;
            socio.setNombre(socioAct.getNombre());
            socio.setApellido(socioAct.getApellido());
            socio.setEmail(socioAct.getEmail());
            socio.setTelefono(socioAct.getTelefono());
            return true;
        }

        try {
            String query = "UPDATE socios SET nombre = ?, apellido = ?, email = ?, telefono = ? WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, socioAct.getNombre());
            ps.setString(2, socioAct.getApellido());
            ps.setString(3, socioAct.getEmail());
            ps.setString(4, socioAct.getTelefono());
            ps.setString(5, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSocio(String id) {
        if (!useDatabase) {
            return socios.removeIf(s -> s.getId().equals(id));
        }

        try {
            String query = "DELETE FROM socios WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
