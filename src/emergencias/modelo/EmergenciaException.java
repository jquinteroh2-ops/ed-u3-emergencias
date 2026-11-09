package emergencias.modelo;

/**
 * Excepción del dominio. Se lanza cuando una operación viola una regla del
 * negocio (código duplicado, tipo inexistente, estado incorrecto, etc.).
 * El menú la captura y muestra el mensaje sin detener el programa.
 */
public class EmergenciaException extends RuntimeException {
    public EmergenciaException(String mensaje) {
        super(mensaje);
    }
}
