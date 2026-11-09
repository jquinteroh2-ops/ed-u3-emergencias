package emergencias.servicio;

import emergencias.modelo.ComparadorPrioridad;
import emergencias.modelo.EmergenciaException;
import emergencias.modelo.EstadoIncidente;
import emergencias.modelo.Gravedad;
import emergencias.modelo.Incidente;
import emergencias.modelo.Recurso;
import emergencias.modelo.TipoEmergencia;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Lógica de negocio de la central de emergencias.
 *
 * Un mismo objeto Incidente puede estar en varias colecciones a la vez; NO son
 * copias, son distintas formas de llegar al mismo objeto. Por eso toda
 * modificación o eliminación pasa por esta clase, que actualiza todas las
 * colecciones afectadas.
 *
 *  Colección                        Para qué sirve
 *  -------------------------------  ---------------------------------------------
 *  List  registroIncidentes         Registro general en orden de llegada.
 *  Map   incidentesPorCodigo        Buscar un incidente por su código en O(1).
 *  Set   tiposEmergencia            Tipos de emergencia sin duplicados.
 *  TreeMap incidentesPorZona        Zonas ordenadas alfabéticamente -> sus incidentes.
 *  PriorityQueue colaAtencion       Atender primero el incidente más grave.
 *  TreeMap recursosPorCodigo        Recursos (ambulancias, patrullas...) ordenados por código.
 *  Deque historialAtenciones        Atenciones despachadas, la más reciente primero.
 */
public class CentralEmergencias {

    private final List<Incidente> registroIncidentes = new ArrayList<>();
    private final Map<String, Incidente> incidentesPorCodigo = new HashMap<>();
    private final Set<TipoEmergencia> tiposEmergencia = new HashSet<>();
    private final TreeMap<String, List<Incidente>> incidentesPorZona = new TreeMap<>();
    private final PriorityQueue<Incidente> colaAtencion = new PriorityQueue<>(new ComparadorPrioridad());
    private final TreeMap<String, Recurso> recursosPorCodigo = new TreeMap<>();
    private final Deque<Incidente> historialAtenciones = new ArrayDeque<>();

    private long contadorLlegadas = 0;

    // =================================================== TIPOS (Set)

    /**
     * Registra un tipo de emergencia. Set.add() devuelve false si ya existía un
     * tipo "igual" según equals()/hashCode() (mismo nombre normalizado).
     */
    public boolean registrarTipo(String nombre, String recursoRequerido) {
        TipoEmergencia tipo = new TipoEmergencia(nombre, recursoRequerido);
        return tiposEmergencia.add(tipo);
    }

    /** Busca el objeto TipoEmergencia guardado en el Set que tenga ese nombre. */
    public TipoEmergencia obtenerTipo(String nombre) {
        String clave = TipoEmergencia.normalizar(nombre);
        for (TipoEmergencia t : tiposEmergencia) {
            if (TipoEmergencia.normalizar(t.getNombre()).equals(clave)) {
                return t;
            }
        }
        throw new EmergenciaException("El tipo de emergencia \"" + nombre + "\" no está registrado.");
    }

    public Set<TipoEmergencia> getTiposEmergencia() {
        return Collections.unmodifiableSet(tiposEmergencia);
    }

    // ============================================== RECURSOS (TreeMap)

    public void registrarRecurso(String codigo, String descripcion, String tipo) {
        Recurso recurso = new Recurso(codigo, descripcion, tipo);
        if (recursosPorCodigo.containsKey(recurso.getCodigo())) {
            throw new EmergenciaException("Ya existe un recurso con el código " + recurso.getCodigo() + ".");
        }
        recursosPorCodigo.put(recurso.getCodigo(), recurso);
    }

    public Map<String, Recurso> getRecursos() {
        return Collections.unmodifiableMap(recursosPorCodigo);
    }

    // ============================================ REGISTRO (List + Map)

    /**
     * Registra un incidente nuevo. Valida los datos y luego lo agrega a:
     * la lista general, el mapa por código y el TreeMap de zonas.
     */
    public Incidente registrarIncidente(String codigo, String ubicacion, String zona,
                                        String nombreTipo, String gravedadTexto, String reportante) {
        String cod = normalizarCodigo(codigo);
        if (incidentesPorCodigo.containsKey(cod)) {
            throw new EmergenciaException("Ya existe un incidente con el código " + cod + ".");
        }
        String ubi = obligatorio(ubicacion, "La ubicación es obligatoria.");
        String zon = normalizarZona(zona);
        TipoEmergencia tipo = obtenerTipo(nombreTipo);           // valida que el tipo exista
        Gravedad gravedad = Gravedad.desdeTexto(gravedadTexto);  // valida la gravedad
        String rep = obligatorio(reportante, "El nombre de quien reporta es obligatorio.");

        Incidente incidente = new Incidente(cod, ubi, zon, tipo, gravedad, rep, ++contadorLlegadas);
        registroIncidentes.add(incidente);
        incidentesPorCodigo.put(cod, incidente);
        agregarAZona(incidente);
        return incidente;
    }

    public List<Incidente> getRegistroIncidentes() {
        return Collections.unmodifiableList(registroIncidentes);
    }

    // ===================================================== BÚSQUEDA (Map)

    /** Búsqueda directa por clave: no recorre la lista. Devuelve null si no existe. */
    public Incidente buscarPorCodigo(String codigo) {
        return incidentesPorCodigo.get(normalizarCodigoSinValidar(codigo));
    }

    public boolean existeCodigo(String codigo) {
        return incidentesPorCodigo.containsKey(normalizarCodigoSinValidar(codigo));
    }

    private Incidente obtenerExistente(String codigo) {
        Incidente incidente = buscarPorCodigo(codigo);
        if (incidente == null) {
            throw new EmergenciaException("No existe ningún incidente con el código "
                    + normalizarCodigoSinValidar(codigo) + ".");
        }
        return incidente;
    }

    // ================================================= ZONAS (TreeMap)

    private void agregarAZona(Incidente incidente) {
        // computeIfAbsent crea la lista de la zona la primera vez que aparece
        incidentesPorZona.computeIfAbsent(incidente.getZona(), z -> new ArrayList<>()).add(incidente);
    }

    private void quitarDeZona(Incidente incidente) {
        List<Incidente> lista = incidentesPorZona.get(incidente.getZona());
        if (lista != null) {
            lista.remove(incidente);
            if (lista.isEmpty()) {
                incidentesPorZona.remove(incidente.getZona()); // no dejar zonas vacías
            }
        }
    }

    /** Vista de solo lectura del TreeMap: las claves (zonas) salen en orden alfabético. */
    public SortedMap<String, List<Incidente>> getIncidentesPorZona() {
        return Collections.unmodifiableSortedMap(incidentesPorZona);
    }

    // ===================================== COLA DE ATENCIÓN (PriorityQueue)

    /** Envía un incidente reportado a la cola de atención por prioridad. */
    public void enviarACola(String codigo) {
        Incidente incidente = obtenerExistente(codigo);
        if (incidente.getEstado() != EstadoIncidente.REPORTADO) {
            throw new EmergenciaException("Solo se pueden enviar a la cola incidentes en estado REPORTADO. "
                    + incidente.getCodigo() + " está " + incidente.getEstado() + ".");
        }
        incidente.setEstado(EstadoIncidente.EN_ESPERA);
        colaAtencion.offer(incidente);
    }

    /** Consulta (sin sacar) el incidente que se atendería a continuación. */
    public Incidente consultarProximo() {
        return colaAtencion.peek();
    }

    /**
     * Atiende el incidente más prioritario: lo saca de la cola y le asigna el
     * primer recurso disponible del tipo que requiere su emergencia.
     * Si no hay recurso disponible, el incidente NO se pierde: se devuelve a la cola.
     */
    public Incidente atenderSiguiente() {
        Incidente incidente = colaAtencion.poll();
        if (incidente == null) {
            throw new EmergenciaException("No hay incidentes esperando atención.");
        }
        Recurso recurso = buscarRecursoDisponible(incidente.getTipo().getRecursoRequerido());
        if (recurso == null) {
            colaAtencion.offer(incidente);
            throw new EmergenciaException("No hay recursos de tipo " + incidente.getTipo().getRecursoRequerido()
                    + " disponibles para " + incidente.getCodigo() + ". Sigue en la cola.");
        }
        recurso.asignar(incidente);
        incidente.setRecursoAsignado(recurso);
        incidente.setEstado(EstadoIncidente.EN_ATENCION);
        historialAtenciones.push(incidente);
        return incidente;
    }

    private Recurso buscarRecursoDisponible(String tipoRecurso) {
        for (Recurso r : recursosPorCodigo.values()) { // recorre en orden de código
            if (r.getTipo().equals(tipoRecurso) && r.estaDisponible()) {
                return r;
            }
        }
        return null;
    }

    /** Cierra un incidente que estaba en atención y libera su recurso. */
    public void cerrarIncidente(String codigo) {
        Incidente incidente = obtenerExistente(codigo);
        if (incidente.getEstado() != EstadoIncidente.EN_ATENCION) {
            throw new EmergenciaException("Solo se pueden cerrar incidentes EN_ATENCION. "
                    + incidente.getCodigo() + " está " + incidente.getEstado() + ".");
        }
        incidente.getRecursoAsignado().liberar();
        incidente.setEstado(EstadoIncidente.CERRADO);
    }

    /**
     * Copia de la cola ordenada por prioridad, solo para mostrarla.
     * (Recorrer una PriorityQueue con un for NO da el orden de prioridad;
     * solo poll() garantiza ese orden, por eso se vacía una copia.)
     */
    public List<Incidente> colaEnOrdenDeAtencion() {
        PriorityQueue<Incidente> copia = new PriorityQueue<>(colaAtencion);
        List<Incidente> orden = new ArrayList<>();
        while (!copia.isEmpty()) {
            orden.add(copia.poll());
        }
        return orden;
    }

    public Deque<Incidente> getHistorialAtenciones() {
        return historialAtenciones;
    }

    // =================================================== MODIFICACIONES

    /**
     * Cambia la gravedad. Si el incidente está dentro de la PriorityQueue hay
     * que sacarlo ANTES de cambiar el atributo y volverlo a insertar DESPUÉS,
     * porque la cola solo reubica elementos cuando se insertan o se extraen.
     */
    public void modificarGravedad(String codigo, String nuevaGravedad) {
        Incidente incidente = obtenerExistente(codigo);
        Gravedad gravedad = Gravedad.desdeTexto(nuevaGravedad);
        if (incidente.getEstado() == EstadoIncidente.CERRADO) {
            throw new EmergenciaException("No se puede modificar un incidente CERRADO.");
        }
        boolean estabaEnCola = colaAtencion.remove(incidente);
        incidente.setGravedad(gravedad);
        if (estabaEnCola) {
            colaAtencion.offer(incidente);
        }
    }

    /** Cambia ubicación y zona; si cambia la zona, se mueve de lista en el TreeMap. */
    public void modificarUbicacion(String codigo, String nuevaUbicacion, String nuevaZona) {
        Incidente incidente = obtenerExistente(codigo);
        String ubi = obligatorio(nuevaUbicacion, "La ubicación es obligatoria.");
        String zon = normalizarZona(nuevaZona);
        if (!zon.equals(incidente.getZona())) {
            quitarDeZona(incidente);
            incidente.setZona(zon);
            agregarAZona(incidente);
        }
        incidente.setUbicacion(ubi);
    }

    /**
     * Cambia el código (la clave del Map). Se elimina la asociación vieja y se
     * crea la nueva; si no, el mapa seguiría encontrándolo por el código viejo.
     * Se saca de la PriorityQueue mientras cambia porque su hashCode/equals cambian.
     */
    public void modificarCodigo(String codigoActual, String codigoNuevo) {
        Incidente incidente = obtenerExistente(codigoActual);
        String nuevo = normalizarCodigo(codigoNuevo);
        if (nuevo.equals(incidente.getCodigo())) {
            return;
        }
        if (incidentesPorCodigo.containsKey(nuevo)) {
            throw new EmergenciaException("El código " + nuevo + " ya está en uso.");
        }
        boolean estabaEnCola = colaAtencion.remove(incidente);
        incidentesPorCodigo.remove(incidente.getCodigo());
        incidente.setCodigo(nuevo);
        incidentesPorCodigo.put(nuevo, incidente);
        if (estabaEnCola) {
            colaAtencion.offer(incidente);
        }
    }

    // ======================================================= ELIMINACIÓN

    /**
     * Elimina un incidente de TODAS las colecciones donde puede estar y libera
     * el recurso si lo tenía. Después de esto ya no se puede encontrar por
     * ninguna vía.
     */
    public Incidente eliminarIncidente(String codigo) {
        Incidente incidente = obtenerExistente(codigo);
        registroIncidentes.remove(incidente);
        incidentesPorCodigo.remove(incidente.getCodigo());
        quitarDeZona(incidente);
        colaAtencion.remove(incidente);
        historialAtenciones.remove(incidente);
        if (incidente.getRecursoAsignado() != null) {
            if (incidente.getRecursoAsignado().getIncidenteAsignado() == incidente) {
                incidente.getRecursoAsignado().liberar();
            }
            incidente.setRecursoAsignado(null);
        }
        return incidente;
    }

    // ================================================ INFORMACIÓN GENERAL

    public int totalIncidentes() {
        return registroIncidentes.size();
    }

    public int tamanioMapa() {
        return incidentesPorCodigo.size();
    }

    public int tamanioCola() {
        return colaAtencion.size();
    }

    public int totalRecursosDisponibles() {
        int disponibles = 0;
        for (Recurso r : recursosPorCodigo.values()) {
            if (r.estaDisponible()) {
                disponibles++;
            }
        }
        return disponibles;
    }

    /** Verifica que todas las colecciones tengan exactamente los mismos incidentes. */
    public boolean coleccionesConsistentes() {
        if (registroIncidentes.size() != incidentesPorCodigo.size()) {
            return false;
        }
        int enZonas = 0;
        for (List<Incidente> lista : incidentesPorZona.values()) {
            enZonas += lista.size();
            for (Incidente i : lista) {
                if (incidentesPorCodigo.get(i.getCodigo()) != i) {
                    return false;
                }
            }
        }
        for (Incidente i : colaAtencion) {
            if (incidentesPorCodigo.get(i.getCodigo()) != i || i.getEstado() != EstadoIncidente.EN_ESPERA) {
                return false;
            }
        }
        return enZonas == registroIncidentes.size();
    }

    // ======================================================= validaciones

    private static String normalizarCodigo(String codigo) {
        String c = normalizarCodigoSinValidar(codigo);
        if (c.isEmpty()) {
            throw new EmergenciaException("El código del incidente es obligatorio.");
        }
        if (!c.matches("INC-\\d{3,5}")) {
            throw new EmergenciaException("Código inválido: \"" + c + "\". Formato esperado: INC-001.");
        }
        return c;
    }

    private static String normalizarCodigoSinValidar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private static String normalizarZona(String zona) {
        String z = obligatorio(zona, "La zona (barrio o sector) es obligatoria.");
        // Primera letra de cada palabra en mayúscula ("el bosque" -> "El Bosque"), salvo
        // conectores en medio del nombre ("pie de la popa" -> "Pie de la Popa").
        // Así "manga" y "Manga" son la misma clave en el TreeMap de zonas.
        Set<String> conectores = Set.of("de", "del", "la", "las", "los", "el", "y");
        StringBuilder sb = new StringBuilder();
        String[] palabras = z.toLowerCase().split("\\s+");
        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i];
            if (i > 0 && conectores.contains(palabra)) {
                sb.append(palabra);
            } else {
                sb.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1));
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static String obligatorio(String texto, String mensaje) {
        if (texto == null || texto.isBlank()) {
            throw new EmergenciaException(mensaje);
        }
        return texto.trim();
    }
}
