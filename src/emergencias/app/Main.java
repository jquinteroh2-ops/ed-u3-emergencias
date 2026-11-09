package emergencias.app;

import emergencias.servicio.CentralEmergencias;

/** Punto de entrada: crea la central y abre el menú. */
public class Main {
    public static void main(String[] args) {
        new MenuConsola(new CentralEmergencias()).iniciar();
    }
}
