package emergencias.app;

import emergencias.modelo.EmergenciaException;
import emergencias.modelo.Incidente;
import emergencias.modelo.Recurso;
import emergencias.modelo.TipoEmergencia;
import emergencias.servicio.CentralEmergencias;
import emergencias.servicio.DatosPrueba;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Interfaz de consola. Solo pide datos y muestra resultados; todas las reglas
 * y operaciones sobre colecciones están en CentralEmergencias.
 */
public class MenuConsola {

    private final CentralEmergencias central;
    private final Scanner entrada = new Scanner(System.in);

    public MenuConsola(CentralEmergencias central) {
        this.central = central;
    }

    public void iniciar() {
        System.out.println("=========================================================");
        System.out.println("    CENTRAL DE ATENCIÓN DE EMERGENCIAS (caso 25)");
        System.out.println("=========================================================");
        if (leer("¿Cargar datos de prueba? (s/n): ").equalsIgnoreCase("s")) {
            DatosPrueba.cargar(central);
        }
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                ejecutar(opcion);
            } catch (EmergenciaException e) {
                System.out.println("  [!] " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("------------------------- MENÚ -------------------------");
        System.out.println(" 1. Registrar incidente");
        System.out.println(" 2. Ver todos los incidentes (orden de registro)");
        System.out.println(" 3. Buscar incidente por código");
        System.out.println(" 4. Tipos de emergencia (registrar / ver)");
        System.out.println(" 5. Ver incidentes por zona (orden alfabético)");
        System.out.println(" 6. Enviar incidente a la cola de atención");
        System.out.println(" 7. Consultar próximo incidente a atender");
        System.out.println(" 8. Atender siguiente incidente (despachar recurso)");
        System.out.println(" 9. Modificar incidente");
        System.out.println("10. Eliminar incidente");
        System.out.println("11. Información de las colecciones");
        System.out.println("12. Cerrar incidente atendido (liberar recurso)");
        System.out.println("13. Recursos de la central (registrar / ver)");
        System.out.println(" 0. Salir");
    }

    private void ejecutar(int opcion) {
        switch (opcion) {
            case 1 -> registrarIncidente();
            case 2 -> verTodos();
            case 3 -> buscarPorCodigo();
            case 4 -> tipos();
            case 5 -> verPorZona();
            case 6 -> enviarACola();
            case 7 -> consultarProximo();
            case 8 -> atenderSiguiente();
            case 9 -> modificar();
            case 10 -> eliminar();
            case 11 -> informacionColecciones();
            case 12 -> cerrar();
            case 13 -> recursos();
            case 0 -> System.out.println("Central cerrada.");
            default -> System.out.println("  [!] Opción no válida.");
        }
    }

    // ------------------------------------------------------------------ 1
    private void registrarIncidente() {
        System.out.println("\n== Registrar incidente ==");
        System.out.println("Tipos disponibles: " + nombresTipos());
        String codigo = leer("Código (INC-###): ");
        if (central.existeCodigo(codigo)) {
            throw new EmergenciaException("Ya existe un incidente con el código " + codigo.trim().toUpperCase() + ".");
        }
        String ubicacion = leer("Ubicación (dirección): ");
        String zona = leer("Zona o barrio: ");
        String tipo = leer("Tipo de emergencia: ");
        String gravedad = leer("Gravedad (CRITICA, ALTA, MEDIA, BAJA): ");
        String reportante = leer("Reportado por: ");
        Incidente nuevo = central.registrarIncidente(codigo, ubicacion, zona, tipo, gravedad, reportante);
        System.out.println("  [OK] Registrado: " + nuevo);
        System.out.println("       Quedó en: List (registro), HashMap (por código) y TreeMap (zona " + nuevo.getZona() + ").");
        if (leer("¿Enviarlo ya a la cola de atención? (s/n): ").equalsIgnoreCase("s")) {
            central.enviarACola(nuevo.getCodigo());
            System.out.println("  [OK] También quedó en la PriorityQueue de atención.");
        }
    }

    // ------------------------------------------------------------------ 2
    private void verTodos() {
        System.out.println("\n== Registro general (ArrayList, orden de llegada) ==");
        imprimir(central.getRegistroIncidentes());
        if (leer("¿Verlos ordenados por código (orden natural, TreeSet)? (s/n): ").equalsIgnoreCase("s")) {
            // TreeSet usa compareTo() de Incidente: ordena por código
            imprimir(List.copyOf(new TreeSet<>(central.getRegistroIncidentes())));
        }
    }

    // ------------------------------------------------------------------ 3
    private void buscarPorCodigo() {
        String codigo = leer("Código a buscar: ");
        Incidente incidente = central.buscarPorCodigo(codigo);
        if (incidente == null) {
            System.out.println("  [X] No existe un incidente con el código \"" + codigo.trim().toUpperCase()
                    + "\" (HashMap.get devolvió null).");
            return;
        }
        System.out.println("  [OK] " + incidente);
        System.out.println("       Reportado por: " + incidente.getReportante()
                + " | Recurso requerido: " + incidente.getTipo().getRecursoRequerido());
    }

    // ------------------------------------------------------------------ 4
    private void tipos() {
        System.out.println("\n== Tipos de emergencia (HashSet, sin duplicados) ==");
        for (TipoEmergencia t : central.getTiposEmergencia()) {
            System.out.println("  - " + t);
        }
        if (leer("¿Registrar un tipo nuevo? (s/n): ").equalsIgnoreCase("s")) {
            String nombre = leer("Nombre del tipo: ");
            String recurso = leer("Tipo de recurso que lo atiende (AMBULANCIA, BOMBEROS, POLICIA, RESCATE...): ");
            if (central.registrarTipo(nombre, recurso)) {
                System.out.println("  [OK] Tipo registrado.");
            } else {
                System.out.println("  [X] El tipo \"" + nombre.trim() + "\" ya existe. El Set no admitió el duplicado.");
            }
        }
    }

    // ------------------------------------------------------------------ 5
    private void verPorZona() {
        System.out.println("\n== Incidentes por zona (TreeMap: claves en orden alfabético) ==");
        for (Map.Entry<String, List<Incidente>> entrada : central.getIncidentesPorZona().entrySet()) {
            System.out.println("  " + entrada.getKey() + " (" + entrada.getValue().size() + ")");
            for (Incidente i : entrada.getValue()) {
                System.out.println("     " + i);
            }
        }
    }

    // ------------------------------------------------------------------ 6
    private void enviarACola() {
        String codigo = leer("Código del incidente a enviar a la cola: ");
        central.enviarACola(codigo);
        System.out.println("  [OK] Enviado. Incidentes en cola: " + central.tamanioCola());
    }

    // ------------------------------------------------------------------ 7
    private void consultarProximo() {
        Incidente proximo = central.consultarProximo();
        if (proximo == null) {
            System.out.println("  No hay incidentes en espera.");
            return;
        }
        System.out.println("  Próximo a atender (peek): " + proximo);
        System.out.println("\n  Cola completa en orden de atención:");
        int turno = 1;
        for (Incidente i : central.colaEnOrdenDeAtencion()) {
            System.out.println("   " + turno++ + ". " + i);
        }
    }

    // ------------------------------------------------------------------ 8
    private void atenderSiguiente() {
        Incidente atendido = central.atenderSiguiente();
        Recurso r = atendido.getRecursoAsignado();
        System.out.println("  [OK] Atendiendo " + atendido.getCodigo() + " (" + atendido.getGravedad() + ", "
                + atendido.getTipo().getNombre() + ") con " + r.getCodigo() + " - " + r.getDescripcion());
    }

    // ------------------------------------------------------------------ 9
    private void modificar() {
        String codigo = leer("Código del incidente a modificar: ");
        Incidente incidente = central.buscarPorCodigo(codigo);
        if (incidente == null) {
            throw new EmergenciaException("No existe un incidente con el código " + codigo.trim().toUpperCase() + ".");
        }
        System.out.println("  Actual: " + incidente);
        System.out.println("  1. Gravedad   2. Ubicación y zona   3. Código");
        int campo = leerEntero("  ¿Qué desea modificar?: ");
        switch (campo) {
            case 1 -> {
                System.out.println("  Gravedades: CRITICA, ALTA, MEDIA, BAJA");
                central.modificarGravedad(incidente.getCodigo(), leer("  Nueva gravedad: "));
            }
            case 2 -> central.modificarUbicacion(incidente.getCodigo(),
                    leer("  Nueva ubicación: "), leer("  Nueva zona: "));
            case 3 -> central.modificarCodigo(incidente.getCodigo(), leer("  Nuevo código (INC-###): "));
            default -> throw new EmergenciaException("Opción de modificación no válida.");
        }
        System.out.println("  [OK] Modificado: " + incidente);
        System.out.println("       Colecciones consistentes: " + central.coleccionesConsistentes());
    }

    // ----------------------------------------------------------------- 10
    private void eliminar() {
        String codigo = leer("Código del incidente a eliminar: ");
        Incidente eliminado = central.eliminarIncidente(codigo);
        System.out.println("  [OK] Eliminado " + eliminado.getCodigo() + " de la lista, el mapa, la zona,"
                + " la cola y el historial.");
        System.out.println("       ¿Se encuentra por código? " + (central.buscarPorCodigo(eliminado.getCodigo()) != null));
        System.out.println("       Colecciones consistentes: " + central.coleccionesConsistentes());
    }

    // ----------------------------------------------------------------- 11
    private void informacionColecciones() {
        System.out.println("\n== Estado de las colecciones ==");
        System.out.printf("  %-44s %d%n", "ArrayList registroIncidentes:", central.totalIncidentes());
        System.out.printf("  %-44s %d%n", "HashMap incidentesPorCodigo:", central.tamanioMapa());
        System.out.printf("  %-44s %d%n", "HashSet tiposEmergencia:", central.getTiposEmergencia().size());
        System.out.printf("  %-44s %d zonas %s%n", "TreeMap incidentesPorZona:",
                central.getIncidentesPorZona().size(), central.getIncidentesPorZona().keySet());
        System.out.printf("  %-44s %d%n", "PriorityQueue colaAtencion:", central.tamanioCola());
        System.out.printf("  %-44s %d (disponibles: %d)%n", "TreeMap recursosPorCodigo:",
                central.getRecursos().size(), central.totalRecursosDisponibles());
        System.out.printf("  %-44s %d%n", "ArrayDeque historialAtenciones:", central.getHistorialAtenciones().size());
        System.out.println("  Colecciones consistentes entre sí: " + central.coleccionesConsistentes());
        if (!central.getHistorialAtenciones().isEmpty()) {
            System.out.println("  Última atención despachada: " + central.getHistorialAtenciones().peekFirst().getCodigo());
        }
    }

    // ----------------------------------------------------------------- 12
    private void cerrar() {
        String codigo = leer("Código del incidente a cerrar: ");
        central.cerrarIncidente(codigo);
        System.out.println("  [OK] Incidente cerrado y recurso liberado.");
    }

    // ----------------------------------------------------------------- 13
    private void recursos() {
        System.out.println("\n== Recursos (TreeMap ordenado por código) ==");
        for (Recurso r : central.getRecursos().values()) {
            System.out.println("  " + r);
        }
        if (leer("¿Registrar un recurso nuevo? (s/n): ").equalsIgnoreCase("s")) {
            central.registrarRecurso(leer("Código: "), leer("Descripción: "),
                    leer("Tipo (AMBULANCIA, BOMBEROS, POLICIA, RESCATE...): "));
            System.out.println("  [OK] Recurso registrado.");
        }
    }

    // ------------------------------------------------------------ utilidades
    private void imprimir(List<Incidente> incidentes) {
        if (incidentes.isEmpty()) {
            System.out.println("  (no hay incidentes)");
        }
        for (Incidente i : incidentes) {
            System.out.println("  " + i);
        }
        System.out.println("  Total: " + incidentes.size());
    }

    private String nombresTipos() {
        StringBuilder sb = new StringBuilder();
        for (TipoEmergencia t : new TreeSet<>(central.getTiposEmergencia())) {
            sb.append(t.getNombre()).append(" | ");
        }
        return sb.length() == 0 ? "(ninguno)" : sb.substring(0, sb.length() - 3);
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : "";
    }

    private int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!entrada.hasNextLine()) {
                return 0;
            }
            try {
                return Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Debe escribir un número.");
            }
        }
    }
}
