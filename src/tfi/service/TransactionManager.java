package tfi.service;

import tfi.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Clase para manejar transacciones con retry automático ante deadlocks.
 */
public class TransactionManager {
    
    private static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY_MS = 100;
    private static final int MAX_DELAY_MS = 1000;
    
    private final DatabaseConnection dbConnection;
    
    public TransactionManager() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Ejecuta una operación transaccional con retry automático.
     * @param operation operación a ejecutar
     * @param <T> tipo de retorno
     * @return resultado de la operación
     * @throws ServiceException si la operación falla después de todos los reintentos
     */
    public <T> T executeWithRetry(TransactionalOperation<T> operation) throws ServiceException {
        int attempt = 0;
        ServiceException lastException = null;
        
        while (attempt < MAX_RETRIES) {
            Connection connection = null;
            try {
                // Obtener nueva conexión para cada intento
                connection = dbConnection.getConnection();
                connection.setAutoCommit(false);
                
                // Ejecutar la operación
                T result = operation.execute(connection);
                
                // Si llegamos aquí, la operación fue exitosa
                connection.commit();
                return result;
                
            } catch (SQLException e) {
                // Manejar rollback
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        // Log rollback error but don't throw
                        System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
                    }
                }
                
                // Determinar si es un error que se puede reintentar
                if (isRetryableError(e)) {
                    attempt++;
                    lastException = new ServiceException(
                        "Error en intento " + attempt + " de " + MAX_RETRIES + ": " + e.getMessage(),
                        e,
                        getErrorType(e)
                    );
                    
                    if (attempt < MAX_RETRIES) {
                        // Esperar antes del siguiente intento (backoff exponencial con jitter)
                        int delay = calculateDelay(attempt);
                        System.out.println("Reintentando en " + delay + "ms...");
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new ServiceException("Operación interrumpida", ie, ServiceException.ErrorType.TRANSACTION_ERROR);
                        }
                    }
                } else {
                    // Error no recuperable, lanzar inmediatamente
                    throw new ServiceException("Error no recuperable en base de datos", e, ServiceException.ErrorType.DATABASE_ERROR);
                }
                
            } catch (ServiceException e) {
                // Rollback en caso de ServiceException
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("Error al hacer rollback: " + rollbackEx.getMessage());
                    }
                }
                
                if (e.isRetryable() && attempt < MAX_RETRIES - 1) {
                    attempt++;
                    lastException = e;
                    int delay = calculateDelay(attempt);
                    System.out.println("Reintentando en " + delay + "ms...");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ServiceException("Operación interrumpida", ie, ServiceException.ErrorType.TRANSACTION_ERROR);
                    }
                } else {
                    throw e;
                }
                
            } finally {
                // Cerrar conexión
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        System.err.println("Error al cerrar la conexión: " + e.getMessage());
                    }
                }
            }
        }
        
        // Si llegamos aquí, todos los intentos fallaron
        if (lastException != null) {
            throw new ServiceException(
                "Operación falló después de " + MAX_RETRIES + " intentos",
                lastException,
                ServiceException.ErrorType.TRANSACTION_ERROR
            );
        } else {
            throw new ServiceException("Operación falló por razones desconocidas", ServiceException.ErrorType.TRANSACTION_ERROR);
        }
    }
    
    /**
     * Determina si un error SQL es recuperable (deadlock, timeout, etc.).
     */
    private boolean isRetryableError(SQLException e) {
        int errorCode = e.getErrorCode();
        String sqlState = e.getSQLState();
        
        // MySQL deadlock
        if (errorCode == 1213 || "40001".equals(sqlState)) {
            return true;
        }
        
        // MySQL lock wait timeout
        if (errorCode == 1205 || "41000".equals(sqlState)) {
            return true;
        }
        
        // Connection timeout o connection lost
        if (errorCode == 0 && (sqlState == null || sqlState.startsWith("08"))) {
            return true;
        }
        
        // Timeout general
        if (errorCode == 2006 || "HY000".equals(sqlState)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Determina el tipo de error basado en la excepción SQL.
     */
    private ServiceException.ErrorType getErrorType(SQLException e) {
        int errorCode = e.getErrorCode();
        String sqlState = e.getSQLState();
        
        if (errorCode == 1213 || "40001".equals(sqlState)) {
            return ServiceException.ErrorType.DEADLOCK_ERROR;
        }
        
        if (errorCode == 1205 || "41000".equals(sqlState)) {
            return ServiceException.ErrorType.TRANSACTION_ERROR;
        }
        
        if (errorCode == 0 && (sqlState == null || sqlState.startsWith("08"))) {
            return ServiceException.ErrorType.CONNECTION_ERROR;
        }
        
        return ServiceException.ErrorType.DATABASE_ERROR;
    }
    
    /**
     * Calcula el delay para el siguiente intento (backoff exponencial con jitter).
     */
    private int calculateDelay(int attempt) {
        int exponentialDelay = BASE_DELAY_MS * (1 << (attempt - 1));
        int delay = Math.min(exponentialDelay, MAX_DELAY_MS);
        
        // Agregar jitter aleatorio para evitar thundering herd
        int jitter = ThreadLocalRandom.current().nextInt(0, delay / 2);
        return delay + jitter;
    }
    
    /**
     * Interfaz funcional para operaciones transaccionales.
     */
    @FunctionalInterface
    public interface TransactionalOperation<T> {
        T execute(Connection connection) throws SQLException, ServiceException;
    }
}
