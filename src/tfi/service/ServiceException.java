package tfi.service;

/**
 * Excepción personalizada para errores en la capa de servicios.
 * Encapsula errores de base de datos y validaciones de negocio.
 */
public class ServiceException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ServiceException(Throwable cause) {
        super(cause);
    }
}
