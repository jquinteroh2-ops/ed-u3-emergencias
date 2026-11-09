package emergencias.modelo;

import java.util.Objects;

/**
 * Tipo de emergencia (Incendio, Accidente de tránsito, ...).
 *
 * Los tipos se guardan en un Set para que no se repitan. Para eso, equals() y
 * hashCode() usan SOLO el nombre normalizado (sin espacios extra y en
 * mayúsculas): "incendio", "Incendio" e " INCENDIO " son el mismo tipo.
 */
public class TipoEmergencia implements Comparable<TipoEmergencia> {

    private final String nombre;
    private final String recursoRequerido;

    public TipoEmergencia(String nombre, String recursoRequerido) {
        if (nombre == null || nombre.isBlank()) {
            throw new EmergenciaException("El nombre del tipo de emergencia es obligatorio.");
        }
        if (recursoRequerido == null || recursoRequerido.isBlank()) {
            throw new EmergenciaException("Debe indicar el tipo de recurso que atiende esta emergencia.");
        }
        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.recursoRequerido = recursoRequerido.trim().toUpperCase();
    }

    public String getNombre() {
        return nombre;
    }

    public String getRecursoRequerido() {
        return recursoRequerido;
    }

    /** Clave usada para comparar: nombre sin espacios sobrantes y en mayúsculas. */
    public static String normalizar(String nombre) {
        return nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TipoEmergencia otro)) {
            return false;
        }
        return normalizar(nombre).equals(normalizar(otro.nombre));
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizar(nombre)); // coherente con equals()
    }

    @Override
    public int compareTo(TipoEmergencia otro) {
        return normalizar(nombre).compareTo(normalizar(otro.nombre));
    }

    @Override
    public String toString() {
        return nombre + " (recurso: " + recursoRequerido + ")";
    }
}
