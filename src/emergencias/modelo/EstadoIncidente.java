package emergencias.modelo;

/** Ciclo de vida de un incidente dentro de la central. */
public enum EstadoIncidente {
    /** Registrado, todavía no se ha enviado a la cola de atención. */
    REPORTADO,
    /** Está dentro de la PriorityQueue esperando un recurso. */
    EN_ESPERA,
    /** Ya tiene un recurso asignado que lo está atendiendo. */
    EN_ATENCION,
    /** La atención terminó y el recurso quedó libre. */
    CERRADO
}
