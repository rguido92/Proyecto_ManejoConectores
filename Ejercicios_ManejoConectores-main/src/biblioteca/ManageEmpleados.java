package biblioteca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageEmpleados {
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet resultSet;
    private boolean useDatabase = false;
    private ArrayList<Empleado> empleados;

    public ManageEmpleados() {
        empleados = new ArrayList<>();
        empleados.add(new Empleado("E001", "Carlos", "Rodríguez", "carlos@example.com", "Bibliotecario", "1800", "2024-01-10"));
        empleados.add(new Empleado("E002", "Ana", "Fernández", "ana@example.com", "Asistente", "1200", "2024-06-15"));
        empleados.add(new Empleado("E003", "Luis", "Sánchez", "luis@example.com", "Administrador", "2000", "2023-03-20"));
    }

    public void openConnection(String bd, String host, String user, String password) {
        try {
            String url = String.format("jdbc:mysql://%s:3306/%s", host, bd);
            this.connection = DriverManager.getConnection(url, user, password);
            useDatabase = true;
            System.out.println("✅ ManageEmpleados: Conectado a BD");
            initDB();
        } catch (SQLException e) {
            useDatabase = false;
            System.out.println("⚠️ ManageEmpleados: Usando almacenamiento en memoria");
        }
    }

    private void initDB() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS empleados (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "apellido VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "puesto VARCHAR(100), " +
                    "salario VARCHAR(20), " +
                    "fecha_contratacion DATE)";
            ps = connection.prepareStatement(createTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Empleado> getAllEmpleados() {
        if (!useDatabase) {
            return new ArrayList<>(empleados);
        }

        ArrayList<Empleado> lista = new ArrayList<>();
        try {
            String query = "SELECT * FROM empleados";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Empleado e = new Empleado(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("email"),
                        resultSet.getString("puesto"),
                        resultSet.getString("salario"),
                        resultSet.getString("fecha_contratacion")
                );
                lista.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Empleado getEmpleadoById(String id) {
        if (!useDatabase) {
            return empleados.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        try {
            String query = "SELECT * FROM empleados WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return new Empleado(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("email"),
                        resultSet.getString("puesto"),
                        resultSet.getString("salario"),
                        resultSet.getString("fecha_contratacion")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addEmpleado(Empleado empleado) {
        if (!useDatabase) {
            if (getEmpleadoById(empleado.getId()) != null) {
                return false;
            }
            return empleados.add(empleado);
        }

        try {
            String query = "INSERT INTO empleados (id, nombre, apellido, email, puesto, salario, fecha_contratacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(query);
            ps.setString(1, empleado.getId());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getApellido());
            ps.setString(4, empleado.getEmail());
            ps.setString(5, empleado.getPuesto());
            ps.setString(6, empleado.getSalario());
            ps.setString(7, empleado.getFechaContratacion());
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmpleado(String id, Empleado empleadoAct) {
        if (!useDatabase) {
            Empleado empleado = getEmpleadoById(id);
            if (empleado == null) return false;
            empleado.setNombre(empleadoAct.getNombre());
            empleado.setApellido(empleadoAct.getApellido());
            empleado.setEmail(empleadoAct.getEmail());
            empleado.setPuesto(empleadoAct.getPuesto());
            empleado.setSalario(empleadoAct.getSalario());
            return true;
        }

        try {
            String query = "UPDATE empleados SET nombre = ?, apellido = ?, email = ?, puesto = ?, salario = ? WHERE id = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, empleadoAct.getNombre());
            ps.setString(2, empleadoAct.getApellido());
            ps.setString(3, empleadoAct.getEmail());
            ps.setString(4, empleadoAct.getPuesto());
            ps.setString(5, empleadoAct.getSalario());
            ps.setString(6, id);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmpleado(String id) {
        if (!useDatabase) {
            return empleados.removeIf(e -> e.getId().equals(id));
        }

        try {
            String query = "DELETE FROM empleados WHERE id = ?";
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
