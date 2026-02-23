package biblioteca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageLibros {
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet resultSet;
    private boolean useDatabase = false;
    private ArrayList<Libro> libros;

    public ManageLibros() {
        libros = new ArrayList<>();
        libros.add(new Libro("L001", "Don Quijote", "Miguel de Cervantes", "978-8437605807", true));
        libros.add(new Libro("L002", "Cien años de soledad", "Gabriel García Márquez", "978-6073128094", true));
        libros.add(new Libro("L003", "La casa de los espíritus", "Isabel Allende", "978-8432217876", false));
        libros.add(new Libro("L004", "Ficciones", "Jorge Luis Borges", "978-8437607499", true));
        libros.add(new Libro("L005", "El quijote de la mancha", "Miguel de Cervantes", "978-8467054735", false));
    }

    public void openConnection(String bd, String host, String user, String password) {
        try {
            String url = String.format("jdbc:mysql://%s:3306/%s", host, bd);
            this.connection = DriverManager.getConnection(url, user, password);
            useDatabase = true;
            System.out.println("✅ ManageLibros: Conectado a BD");
            initDB();
        } catch (SQLException e) {
            useDatabase = false;
            System.out.println("⚠️ ManageLibros: Usando almacenamiento en memoria");
        }
    }

    private void initDB() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS libros (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "titulo VARCHAR(200) NOT NULL, " +
                    "autor VARCHAR(100), " +
                    "isbn VARCHAR(20), " +
                    "disponible BOOLEAN DEFAULT 1)";
            ps = connection.prepareStatement(createTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Libro> getAllLibros() {
        if (!useDatabase) {
            return new ArrayList<>(libros);
        }

        ArrayList<Libro> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM libros";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Libro l = new Libro(
                        resultSet.getString("id"),
                        resultSet.getString("titulo"),
                        resultSet.getString("autor"),
                        resultSet.getString("isbn"),
                        resultSet.getBoolean("disponible")
                );
                lista.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<Libro> getLibrosDisponibles() {
        if (!useDatabase) {
            ArrayList<Libro> disponibles = new ArrayList<>();
            for (Libro libro : libros) {
                if (libro.isDisponible()) {
                    disponibles.add(libro);
                }
            }
            return disponibles;
        }

        ArrayList<Libro> disponibles = new ArrayList<>();
        try {
            String query = "SELECT * FROM libros WHERE disponible = true";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Libro l = new Libro(
                        resultSet.getString("id"),
                        resultSet.getString("titulo"),
                        resultSet.getString("autor"),
                        resultSet.getString("isbn"),
                        resultSet.getBoolean("disponible")
                );
                disponibles.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return disponibles;
    }

    public ArrayList<Libro> getLibrosAlquilados() {
        if (!useDatabase) {
            ArrayList<Libro> alquilados = new ArrayList<>();
            for (Libro libro : libros) {
                if (!libro.isDisponible()) {
                    alquilados.add(libro);
                }
            }
            return alquilados;
        }

        ArrayList<Libro> alquilados = new ArrayList<>();
        try {
            String query = "SELECT * FROM libros WHERE disponible = false";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Libro l = new Libro(
                        resultSet.getString("id"),
                        resultSet.getString("titulo"),
                        resultSet.getString("autor"),
                        resultSet.getString("isbn"),
                        resultSet.getBoolean("disponible")
                );
                alquilados.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alquilados;
    }

    public Libro getLibroById(String id) {
        if (!useDatabase) {
            return libros.stream()
                    .filter(l -> l.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        try {
            String query = "SELECT * FROM libros WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return new Libro(
                        resultSet.getString("id"),
                        resultSet.getString("titulo"),
                        resultSet.getString("autor"),
                        resultSet.getString("isbn"),
                        resultSet.getBoolean("disponible")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addLibro(Libro libro) {
        if (!useDatabase) {
            if (getLibroById(libro.getId()) != null) {
                return false;
            }
            return libros.add(libro);
        }

        try {
            String query = "INSERT INTO libros (id, titulo, autor, isbn, disponible) VALUES (?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(query);
            ps.setString(1, libro.getId());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setString(4, libro.getIsbn());
            ps.setBoolean(5, libro.isDisponible());
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLibro(String id, Libro libroAct) {
        if (!useDatabase) {
            Libro libro = getLibroById(id);
            if (libro == null) return false;
            libro.setTitulo(libroAct.getTitulo());
            libro.setAutor(libroAct.getAutor());
            libro.setIsbn(libroAct.getIsbn());
            libro.setDisponible(libroAct.isDisponible());
            return true;
        }

        try {
            String query = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, disponible = ? WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, libroAct.getTitulo());
            ps.setString(2, libroAct.getAutor());
            ps.setString(3, libroAct.getIsbn());
            ps.setBoolean(4, libroAct.isDisponible());
            ps.setString(5, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteLibro(String id) {
        if (!useDatabase) {
            return libros.removeIf(l -> l.getId().equals(id));
        }

        try {
            String query = "DELETE FROM libros WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDisponibilidad(String id, boolean disponible) {
        if (!useDatabase) {
            Libro libro = getLibroById(id);
            if (libro != null) {
                libro.setDisponible(disponible);
                return true;
            }
            return false;
        }

        try {
            String query = "UPDATE libros SET disponible = ? WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setBoolean(1, disponible);
            ps.setString(2, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
