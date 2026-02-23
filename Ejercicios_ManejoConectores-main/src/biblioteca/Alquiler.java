package biblioteca;

public class Alquiler {
    private String id;
    private String socioId;
    private String libroId;
    private String fechaAlquiler;
    private String fechaDevolucion;
    private String estado;

    public Alquiler() {
    }

    public Alquiler(String id, String socioId, String libroId, String fechaAlquiler, String fechaDevolucion, String estado) {
        this.id = id;
        this.socioId = socioId;
        this.libroId = libroId;
        this.fechaAlquiler = fechaAlquiler;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSocioId() {
        return socioId;
    }

    public void setSocioId(String socioId) {
        this.socioId = socioId;
    }

    public String getLibroId() {
        return libroId;
    }

    public void setLibroId(String libroId) {
        this.libroId = libroId;
    }

    public String getFechaAlquiler() {
        return fechaAlquiler;
    }

    public void setFechaAlquiler(String fechaAlquiler) {
        this.fechaAlquiler = fechaAlquiler;
    }

    public String getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(String fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Alquiler{" +
                "id='" + id + '\'' +
                ", socioId='" + socioId + '\'' +
                ", libroId='" + libroId + '\'' +
                ", fechaAlquiler='" + fechaAlquiler + '\'' +
                ", fechaDevolucion='" + fechaDevolucion + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
