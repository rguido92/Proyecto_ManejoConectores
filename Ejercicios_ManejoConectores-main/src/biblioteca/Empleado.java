package biblioteca;

public class Empleado {
    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private String puesto;
    private String salario;
    private String fechaContratacion;

    public Empleado() {
    }

    public Empleado(String id, String nombre, String apellido, String email, String puesto, String salario, String fechaContratacion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.puesto = puesto;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getSalario() {
        return salario;
    }

    public void setSalario(String salario) {
        this.salario = salario;
    }

    public String getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(String fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", puesto='" + puesto + '\'' +
                ", salario='" + salario + '\'' +
                ", fechaContratacion='" + fechaContratacion + '\'' +
                '}';
    }
}
