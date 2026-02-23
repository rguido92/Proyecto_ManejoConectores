package biblioteca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ManageAlquileres {
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet resultSet;
    private boolean useDatabase = false;
    private ArrayList<Alquiler> alquileres;
    private ManageLibros manageLibros = new ManageLibros();

    public ManageAlquileres() {
        alquileres = new ArrayList<>();
        alquileres.add(new Alquiler("A001", "S001", "L002", "2025-02-01", null, "activo"));
        alquileres.add(new Alquiler("A002", "S002", "L003", "2025-02-03", null, "activo"));
        alquileres.add(new Alquiler("A003", "S003", "L005", "2025-02-05", null, "activo"));
        alquileres.add(new Alquiler("A004", "S001", "L001", "2025-01-20", "2025-02-05", "devuelto"));
    }

    public void openConnection(String bd, String host, String user, String password) {
        try {
            String url = String.format("jdbc:mysql://%s:3306/%s", host, bd);
            this.connection = DriverManager.getConnection(url, user, password);
            useDatabase = true;
            System.out.println("✅ ManageAlquileres: Conectado a BD");
            initDB();
        } catch (SQLException e) {
            useDatabase = false;
            System.out.println("⚠️ ManageAlquileres: Usando almacenamiento en memoria");
        }
    }

    private void initDB() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS alquileres (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "socio_id VARCHAR(36), " +
                    "libro_id VARCHAR(36), " +
                    "fecha_alquiler DATE, " +
                    "fecha_devolucion DATE, " +
                    "estado VARCHAR(20))";
            ps = connection.prepareStatement(createTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Alquiler> getAllAlquileres() {
        if (!useDatabase) {
            return new ArrayList<>(alquileres);
        }

        ArrayList<Alquiler> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM alquileres";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Alquiler a = new Alquiler(
                        resultSet.getString("id"),
                        resultSet.getString("socio_id"),
                        resultSet.getString("libro_id"),
                        resultSet.getString("fecha_alquiler"),
                        resultSet.getString("fecha_devolucion"),
                        resultSet.getString("estado")
                );
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<Alquiler> getAlquileresActivos() {
        if (!useDatabase) {
            ArrayList<Alquiler> activos = new ArrayList<>();
            for (Alquiler alquiler : alquileres) {
                if ("activo".equals(alquiler.getEstado())) {
                    activos.add(alquiler);
                }
            }
            return activos;
        }

        ArrayList<Alquiler> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM alquileres WHERE estado = 'activo'";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Alquiler a = new Alquiler(
                        resultSet.getString("id"),
                        resultSet.getString("socio_id"),
                        resultSet.getString("libro_id"),
                        resultSet.getString("fecha_alquiler"),
                        resultSet.getString("fecha_devolucion"),
                        resultSet.getString("estado")
                );
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<Alquiler> getHistorialAlquileres() {
        if (!useDatabase) {
            ArrayList<Alquiler> historial = new ArrayList<>();
            for (Alquiler alquiler : alquileres) {
                if ("devuelto".equals(alquiler.getEstado())) {
                    historial.add(alquiler);
                }
            }
            return historial;
        }

        ArrayList<Alquiler> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM alquileres WHERE estado = 'devuelto'";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Alquiler a = new Alquiler(
                        resultSet.getString("id"),
                        resultSet.getString("socio_id"),
                        resultSet.getString("libro_id"),
                        resultSet.getString("fecha_alquiler"),
                        resultSet.getString("fecha_devolucion"),
                        resultSet.getString("estado")
                );
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Alquiler getAlquilerById(String id) {
        if (!useDatabase) {
            return alquileres.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        try {
            String query = "SELECT * FROM alquileres WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return new Alquiler(
                        resultSet.getString("id"),
                        resultSet.getString("socio_id"),
                        resultSet.getString("libro_id"),
                        resultSet.getString("fecha_alquiler"),
                        resultSet.getString("fecha_devolucion"),
                        resultSet.getString("estado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registrarAlquiler(Alquiler alquiler) {
        if (!useDatabase) {
            if (getAlquilerById(alquiler.getId()) != null) {
                return false;
            }
            boolean resultado = alquileres.add(alquiler);
            if (resultado) {
                manageLibros.setDisponibilidad(alquiler.getLibroId(), false);
            }
            return resultado;
        }

        try {
            String query = "INSERT INTO alquileres (id, socio_id, libro_id, fecha_alquiler, fecha_devolucion, estado) VALUES (?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(query);
            ps.setString(1, alquiler.getId());
            ps.setString(2, alquiler.getSocioId());
            ps.setString(3, alquiler.getLibroId());
            ps.setString(4, alquiler.getFechaAlquiler());
            ps.setString(5, alquiler.getFechaDevolucion());
            ps.setString(6, alquiler.getEstado());
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean devolverLibro(String alquilerId) {
        Alquiler alquiler = getAlquilerById(alquilerId);
        if (alquiler == null) return false;

        String fechaDev = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        if (!useDatabase) {
            alquiler.setEstado("devuelto");
            alquiler.setFechaDevolucion(fechaDev);
            manageLibros.setDisponibilidad(alquiler.getLibroId(), true);
            return true;
        }

        try {
            String query = "UPDATE alquileres SET estado = 'devuelto', fecha_devolucion = ? WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, fechaDev);
            ps.setString(2, alquilerId);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAlquiler(String id) {
        if (!useDatabase) {
            return alquileres.removeIf(a -> a.getId().equals(id));
        }

        try {
            String query = "DELETE FROM alquileres WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Alquiler> getAlquileresDelSocio(String socioId) {
        if (!useDatabase) {
            ArrayList<Alquiler> resultado = new ArrayList<>();
            for (Alquiler alquiler : alquileres) {
                if (alquiler.getSocioId().equals(socioId)) {
                    resultado.add(alquiler);
                }
            }
            return resultado;
        }

        ArrayList<Alquiler> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM alquileres WHERE socio_id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, socioId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Alquiler a = new Alquiler(
                        resultSet.getString("id"),
                        resultSet.getString("socio_id"),
                        resultSet.getString("libro_id"),
                        resultSet.getString("fecha_alquiler"),
                        resultSet.getString("fecha_devolucion"),
                        resultSet.getString("estado")
                );
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
