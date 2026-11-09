package emergencias.modelo;

import java.util.Comparator;

/**
 * Criterio de la PriorityQueue de atención.
 *
 * 1. Primero la gravedad: CRITICA (nivel 1) antes que ALTA (2), MEDIA (3) y BAJA (4).
 * 2. Si la gravedad es igual, primero el que llegó antes (menor numeroLlegada).
 *
 * compare() devuelve un número negativo cuando "a" debe atenderse antes que "b".
 * La PriorityQueue siempre deja en la cabeza (peek/poll) el elemento "menor"
 * según este comparador.
 */
public class ComparadorPrioridad implements Comparator<Incidente> {

    @Override
    public int compare(Incidente a, Incidente b) {
        int porGravedad = Integer.compare(a.getGravedad().getNivel(), b.getGravedad().getNivel());
        if (porGravedad != 0) {
            return porGravedad;
        }
        return Long.compare(a.getNumeroLlegada(), b.getNumeroLlegada());
    }
}
