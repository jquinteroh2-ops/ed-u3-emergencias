package emergencias.modelo;

import java.util.Objects;

/**
 * Recurso de la central que se despacha a un incidente: una ambulancia, una
 * máquina de bomberos, una patrulla, etc. Se identifica por su código.
 */
public class Recurso implements Comparable<Recurso> {

    private final String codigo;
    private final String descripcion;
    private final String tipo;
    private Incidente incidenteAsignado;

    public Recurso(String codigo, String descripcion, String tipo) {
        if (codigo == null || codigo.isBlank()) {
            throw new EmergenciaException("El código del recurso es obligatorio.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new EmergenciaException("El tipo de recurso es obligatorio.");
        }
        this.codigo = codigo.trim().toUpperCase();
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.tipo = tipo.trim().toUpperCase();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean estaDisponible() {
        return incidenteAsignado == null;
    }

    public Incidente getIncidenteAsignado() {
        return incidenteAsignado;
    }

    public void asignar(Incidente incidente) {
        this.incidenteAsignado = incidente;
    }

    public void liberar() {
        this.incidenteAsignado = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recurso otro)) {
            return false;
        }
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public int compareTo(Recurso otro) {
        return codigo.compareTo(otro.codigo);
    }

    @Override
    public String toString() {
        String estado = estaDisponible() ? "DISPONIBLE" : "OCUPADO en " + incidenteAsignado.getCodigo();
        return String.format("%-7s %-11s %-30s %s", codigo, tipo, descripcion, estado);
    }
}
