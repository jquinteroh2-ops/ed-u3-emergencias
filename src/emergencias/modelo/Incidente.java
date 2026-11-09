package emergencias.modelo;

import java.util.Objects;

/**
 * Incidente reportado a la central de emergencias.
 *
 * Identidad: el CÓDIGO. Dos incidentes con el mismo código son el mismo
 * incidente; por eso equals() y hashCode() usan solo ese atributo.
 *
 * Orden natural (Comparable): por código, para listarlos ordenados por código.
 * El orden de ATENCIÓN no es el natural; lo define ComparadorPrioridad.
 */
public class Incidente implements Comparable<Incidente> {

    private String codigo;
    private String ubicacion;
    private String zona;
    private final TipoEmergencia tipo;
    private Gravedad gravedad;
    private EstadoIncidente estado;
    private final String reportante;
    private final long numeroLlegada;   // orden de registro, desempata prioridades iguales
    private Recurso recursoAsignado;

    public Incidente(String codigo, String ubicacion, String zona, TipoEmergencia tipo,
                     Gravedad gravedad, String reportante, long numeroLlegada) {
        this.codigo = codigo;
        this.ubicacion = ubicacion;
        this.zona = zona;
        this.tipo = tipo;
        this.gravedad = gravedad;
        this.reportante = reportante;
        this.numeroLlegada = numeroLlegada;
        this.estado = EstadoIncidente.REPORTADO;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Solo la central debe llamarlo, porque también debe actualizar el Map. */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getZona() {
        return zona;
    }

    /** Solo la central debe llamarlo, porque también debe actualizar el TreeMap de zonas. */
    public void setZona(String zona) {
        this.zona = zona;
    }

    public TipoEmergencia getTipo() {
        return tipo;
    }

    public Gravedad getGravedad() {
        return gravedad;
    }

    /** Solo la central debe llamarlo, porque puede afectar la PriorityQueue. */
    public void setGravedad(Gravedad gravedad) {
        this.gravedad = gravedad;
    }

    public EstadoIncidente getEstado() {
        return estado;
    }

    public void setEstado(EstadoIncidente estado) {
        this.estado = estado;
    }

    public String getReportante() {
        return reportante;
    }

    public long getNumeroLlegada() {
        return numeroLlegada;
    }

    public Recurso getRecursoAsignado() {
        return recursoAsignado;
    }

    public void setRecursoAsignado(Recurso recursoAsignado) {
        this.recursoAsignado = recursoAsignado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Incidente otro)) {
            return false;
        }
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public int compareTo(Incidente otro) {
        return codigo.compareTo(otro.codigo);
    }

    @Override
    public String toString() {
        String recurso = recursoAsignado == null ? "-" : recursoAsignado.getCodigo();
        return String.format("%-8s llegada#%-3d %-8s %-22s %-13s %-28s %-11s recurso:%s",
                codigo, numeroLlegada, gravedad, tipo.getNombre(), zona, ubicacion, estado, recurso);
    }
}
