package emergencias.modelo;

/**
 * Nivel de gravedad de un incidente.
 * El número "nivel" define la prioridad de atención: 1 se atiende primero.
 */
public enum Gravedad {
    CRITICA(1, "Riesgo inminente para la vida"),
    ALTA(2, "Lesionados o daños graves"),
    MEDIA(3, "Situación controlable, requiere apoyo"),
    BAJA(4, "Sin lesionados, atención rutinaria");

    private final int nivel;
    private final String descripcion;

    Gravedad(int nivel, String descripcion) {
        this.nivel = nivel;
        this.descripcion = descripcion;
    }

    public int getNivel() {
        return nivel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Convierte el texto del usuario (CRITICA, critica, 1...) en un valor del enum. */
    public static Gravedad desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new EmergenciaException("La gravedad es obligatoria.");
        }
        String t = texto.trim().toUpperCase().replace("Í", "I");
        for (Gravedad g : values()) {
            if (g.name().equals(t) || String.valueOf(g.nivel).equals(t)) {
                return g;
            }
        }
        throw new EmergenciaException("Gravedad inválida: \"" + texto
                + "\". Use CRITICA, ALTA, MEDIA o BAJA (o 1 a 4).");
    }
}
