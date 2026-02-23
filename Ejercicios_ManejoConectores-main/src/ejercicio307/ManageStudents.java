package ejercicio307;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class ManageStudents {
    private Connection connection;
    private PreparedStatement ps;
    private Statement statement;
    private ResultSet resultSet;

    private int numFilasAfectadas;
    private ArrayList<Student> students;
    private Student student;
    private String query;
    private Scanner sc;
    private boolean useDatabase = false; // Flag to indicate if database is available

    public ManageStudents() {
        students = new ArrayList<>();
        // Initialize with sample data
        students.add(new Student("s001", "Juan", "García", 20));
        students.add(new Student("s002", "María", "López", 21));
        students.add(new Student("s003", "Carlos", "Martínez", 22));
    }

    public void openConnection(String bd, String server, String user, String password) {
        try {
            String url = String.format("jdbc:mysql://%s:3306/%s", server, bd);
            this.connection = DriverManager.getConnection(url, user, password);
            if (connection != null) {
                useDatabase = true;
                System.out.println("Conectado a base de datos");
            } else {
                useDatabase = false;
                System.out.println("No se ha podido conectar, usando almacenamiento en memoria");
            }
        } catch (SQLException e) {
            useDatabase = false;
            System.out.println("No se pudo conectar a base de datos, usando almacenamiento en memoria");
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean addStudent(Student student) {
        // If no database, use in-memory storage
        if (!useDatabase) {
            students.add(student);
            return true;
        }

        // Otherwise use JDBC
        query = "INSERT INTO STUDENT VALUES(?,?,?,?)";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, student.getId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getSurname());
            ps.setInt(4, student.getAge());
            numFilasAfectadas = ps.executeUpdate();
            System.out.println("Filas afectadas = " + numFilasAfectadas);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Student> getStudents() {
        if (!useDatabase) {
            return new ArrayList<>(students);
        }

        students = new ArrayList<Student>();
        try {
            query = "SELECT * FROM STUDENT;";
            ps = connection.prepareStatement(query);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                student = new Student(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                        resultSet.getInt(4));
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public Student getStudent(String id) {
        if (!useDatabase) {
            return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
        }

        query = "SELECT * FROM STUDENT WHERE ID=?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            resultSet = ps.executeQuery();
            int columns = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                student = new Student(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                        resultSet.getInt(4));
                return student;
            }
            System.out.println("Filas afectadas = " + numFilasAfectadas);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return student;
    }

    public boolean deleteStudent(String id) {
        if (!useDatabase) {
            return students.removeIf(s -> s.getId().equals(id));
        }

        query = "DELETE FROM STUDENT WHERE ID= ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            numFilasAfectadas = ps.executeUpdate();
            System.out.println("Filas afectadas = " + numFilasAfectadas);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean modifyStudent() {
        sc = new Scanner(System.in);
        System.out.println("Introduce id del estudiante a modificar :");
        String id = sc.nextLine();
        student = createStudent();
        query = "UPDATE STUDENT SET ID = ?,NAME = ?, SURNAME= ?,AGE =? WHERE ID=?";

        try {
            System.out.println("Modificando usuario... ");
            ps = connection.prepareStatement(query);
            ps.setString(1, student.getId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getSurname());
            ps.setInt(4, student.getAge());
            ps.setString(5, id);
            numFilasAfectadas = ps.executeUpdate();
            System.out.println("Filas afectadas = " + numFilasAfectadas);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Server-friendly modify method: updates student with given id using provided Student data.
     */
    public boolean modifyStudent(String id, Student newStudent) {
        if (!useDatabase) {
            // Update in-memory storage
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getId().equals(id)) {
                    // Keep original ID if 'from' id parameter is different
                    newStudent.setId(newStudent.getId() != null && !newStudent.getId().isEmpty() ? newStudent.getId() : id);
                    students.set(i, newStudent);
                    return true;
                }
            }
            return false;
        }

        query = "UPDATE STUDENT SET ID = ?, NAME = ?, SURNAME = ?, AGE = ? WHERE ID = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, newStudent.getId());
            ps.setString(2, newStudent.getName());
            ps.setString(3, newStudent.getSurname());
            ps.setInt(4, newStudent.getAge());
            ps.setString(5, id);
            numFilasAfectadas = ps.executeUpdate();
            System.out.println("Filas afectadas = " + numFilasAfectadas);
            return numFilasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    Student createStudent() {
        sc = new Scanner(System.in);
        String id, name, surname;
        int age;
        try {
            System.out.println("Introduce id");
            id = sc.nextLine();
            System.out.println("Introduce nombre");
            name = sc.nextLine();
            System.out.println("Introduce apellido");
            surname = sc.nextLine();
            System.out.println("Introduce edad");
            age = sc.nextInt();
            student = new Student(id, name, surname, age);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return student;

    }

}
