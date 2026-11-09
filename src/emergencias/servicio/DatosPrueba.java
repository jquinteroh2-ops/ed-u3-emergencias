package emergencias.servicio;

import emergencias.modelo.EmergenciaException;

/**
 * Carga datos preparados para probar todas las colecciones.
 *
 * - 6 tipos de emergencia + 2 intentos de tipo duplicado (Set).
 * - 9 recursos (TreeMap ordenado por código).
 * - 16 incidentes en 7 zonas (List, Map y TreeMap de zonas).
 * - 10 incidentes enviados a la cola en un orden distinto a su gravedad
 *   (PriorityQueue): el primero que llega es BAJA y el último es CRITICA.
 * - 1 intento de incidente con código repetido (Map).
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static void cargar(CentralEmergencias central) {
        central.registrarTipo("Incendio", "BOMBEROS");
        central.registrarTipo("Accidente de tránsito", "AMBULANCIA");
        central.registrarTipo("Emergencia médica", "AMBULANCIA");
        central.registrarTipo("Hurto", "POLICIA");
        central.registrarTipo("Inundación", "RESCATE");
        central.registrarTipo("Riña", "POLICIA");
        boolean dup1 = central.registrarTipo("INCENDIO", "BOMBEROS");          // duplicado
        boolean dup2 = central.registrarTipo("  accidente de   tránsito ", "AMBULANCIA"); // duplicado
        System.out.println("  Intento de tipos duplicados aceptados por el Set: " + dup1 + ", " + dup2);

        central.registrarRecurso("AMB-01", "Ambulancia básica Cruz Roja", "AMBULANCIA");
        central.registrarRecurso("AMB-02", "Ambulancia medicalizada", "AMBULANCIA");
        central.registrarRecurso("AMB-03", "Ambulancia Hospital Turbaco", "AMBULANCIA");
        central.registrarRecurso("BOM-01", "Máquina extintora estación 1", "BOMBEROS");
        central.registrarRecurso("BOM-02", "Máquina de rescate estación 2", "BOMBEROS");
        central.registrarRecurso("POL-01", "Patrulla cuadrante 4", "POLICIA");
        central.registrarRecurso("POL-02", "Patrulla motorizada 7", "POLICIA");
        central.registrarRecurso("RES-01", "Bote de rescate Defensa Civil", "RESCATE");
        central.registrarRecurso("RES-02", "Grupo USAR Defensa Civil", "RESCATE");

        // codigo, ubicación, zona, tipo, gravedad, reportante
        central.registrarIncidente("INC-001", "Calle 30 #22-14", "Manga", "Hurto", "BAJA", "Luis Mora");
        central.registrarIncidente("INC-002", "Av. San Martín #8-20", "Bocagrande", "Emergencia médica", "MEDIA", "Hotel Caribe");
        central.registrarIncidente("INC-003", "Cra 17 #25-30", "Turbaco Centro", "Incendio", "ALTA", "Marta Ríos");
        central.registrarIncidente("INC-004", "Transversal 54 #31", "El Bosque", "Accidente de tránsito", "BAJA", "Agente de tránsito");
        central.registrarIncidente("INC-005", "Mercado de Bazurto", "El Bosque", "Incendio", "CRITICA", "Comerciantes");
        central.registrarIncidente("INC-006", "Calle del Arsenal", "Centro", "Riña", "MEDIA", "Policía turística");
        central.registrarIncidente("INC-007", "Av. Pedro de Heredia km 2", "Crespo", "Accidente de tránsito", "CRITICA", "Taxista");
        central.registrarIncidente("INC-008", "Barrio Olaya sector 11", "Olaya Herrera", "Inundación", "ALTA", "Junta de acción comunal");
        central.registrarIncidente("INC-009", "Calle 70 #4-15", "Crespo", "Emergencia médica", "ALTA", "Ana Castro");
        central.registrarIncidente("INC-010", "Plaza de la Trinidad", "Centro", "Hurto", "MEDIA", "Turista");
        central.registrarIncidente("INC-011", "Cra 3 #5-90", "Bocagrande", "Inundación", "BAJA", "Edificio Nautilus");
        central.registrarIncidente("INC-012", "Calle Real #24", "Manga", "Emergencia médica", "CRITICA", "Pedro Gil");
        central.registrarIncidente("INC-013", "Vía Turbaco-Arjona km 1", "Turbaco Centro", "Accidente de tránsito", "MEDIA", "Motociclista");
        central.registrarIncidente("INC-014", "Parque de Olaya", "Olaya Herrera", "Riña", "ALTA", "Vecinos");
        central.registrarIncidente("INC-015", "Centro Comercial Caribe Plaza", "Pie De La Popa", "Hurto", "BAJA", "Seguridad del C.C.");
        central.registrarIncidente("INC-016", "Calle 31 #17-40", "Pie De La Popa", "Emergencia médica", "MEDIA", "Carlos León");

        try {
            central.registrarIncidente("INC-005", "Otra dirección", "Manga", "Hurto", "BAJA", "Prueba");
        } catch (EmergenciaException e) {
            System.out.println("  Intento de código duplicado rechazado: " + e.getMessage());
        }

        // Se envían a la cola en un orden que NO coincide con la gravedad
        String[] alaCola = {"INC-001", "INC-004", "INC-002", "INC-003", "INC-006",
                "INC-005", "INC-009", "INC-010", "INC-007", "INC-012"};
        for (String codigo : alaCola) {
            central.enviarACola(codigo);
        }
        System.out.println("  Cargados: 6 tipos, 9 recursos, 16 incidentes (10 en cola de atención).");
    }
}
