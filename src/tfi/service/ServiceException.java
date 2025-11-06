package tfi.service;

/**
 * Excepción personalizada para errores en la capa de servicios.
 * Encapsula errores de base de datos y validaciones de negocio.
 */
public class ServiceException extends Exception {
    
    private static final long serialVersionUID = 1L;
    private final ErrorType errorType;
    
    public enum ErrorType {
        VALIDATION_ERROR,
        DATABASE_ERROR,
        DEADLOCK_ERROR,
        CONNECTION_ERROR,
        TRANSACTION_ERROR,
        BUSINESS_LOGIC_ERROR
    }
    
    public ServiceException(String message) {
        super(message);
        this.errorType = ErrorType.BUSINESS_LOGIC_ERROR;
    }
    
    public ServiceException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.DATABASE_ERROR;
    }
    
    public ServiceException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public ServiceException(Throwable cause) {
        super(cause);
        this.errorType = ErrorType.DATABASE_ERROR;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public boolean isRetryable() {
        return errorType == ErrorType.DEADLOCK_ERROR || 
               errorType == ErrorType.CONNECTION_ERROR ||
               errorType == ErrorType.TRANSACTION_ERROR;
    }
}
