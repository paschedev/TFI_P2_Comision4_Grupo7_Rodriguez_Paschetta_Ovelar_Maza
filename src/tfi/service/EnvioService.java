package tfi.service;

import tfi.config.DatabaseConnection;
import tfi.dao.EnvioDao;
import tfi.dao.GenericDao;
import tfi.entities.Envio;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para la entidad Envio.
 * Implementa reglas de negocio y validaciones con manejo de transacciones robusto.
 */
public class EnvioService implements GenericService<Envio> {
    
    private final GenericDao<Envio> envioDao;
    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    
    public EnvioService() {
        this.envioDao = new EnvioDao();
        this.dbConnection = DatabaseConnection.getInstance();
        this.transactionManager = new TransactionManager();
    }
    
    @Override
    public Envio insertar(Envio envio) throws ServiceException {
        validarEnvio(envio);
        
        return transactionManager.executeWithRetry(connection -> {
            // Verificar que el tracking no exista
            if (envio.getTracking() != null && !envio.getTracking().trim().isEmpty()) {
                EnvioDao envioDaoImpl = (EnvioDao) envioDao;
                Envio existente = envioDaoImpl.buscarPorTracking(envio.getTracking(), connection);
                if (existente != null) {
                    throw new ServiceException(
                        "Ya existe un envío con el tracking: " + envio.getTracking(),
                        ServiceException.ErrorType.VALIDATION_ERROR
                    );
                }
            }
            
            return envioDao.crear(envio, connection);
        });
    }
    
    @Override
    public Envio actualizar(Envio envio) throws ServiceException {
        validarEnvio(envio);
        
        if (envio.getId() == null) {
            throw new ServiceException(
                "El ID del envío es requerido para actualizar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // Verificar que el envío existe
            if (!envioDao.existe(envio.getId(), connection)) {
                throw new ServiceException(
                    "No existe un envío con ID: " + envio.getId(),
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // Verificar que el tracking no esté en uso por otro envío
            if (envio.getTracking() != null && !envio.getTracking().trim().isEmpty()) {
                EnvioDao envioDaoImpl = (EnvioDao) envioDao;
                Envio existente = envioDaoImpl.buscarPorTracking(envio.getTracking(), connection);
                if (existente != null && !existente.getId().equals(envio.getId())) {
                    throw new ServiceException(
                        "Ya existe otro envío con el tracking: " + envio.getTracking(),
                        ServiceException.ErrorType.VALIDATION_ERROR
                    );
                }
            }
            
            return envioDao.actualizar(envio, connection);
        });
    }
    
    @Override
    public boolean eliminar(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException(
                "El ID del envío es requerido para eliminar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // Verificar que el envío existe
            if (!envioDao.existe(id, connection)) {
                throw new ServiceException(
                    "No existe un envío con ID: " + id,
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            return envioDao.eliminar(id, connection);
        });
    }
    
    @Override
    public Envio getById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException(
                "El ID del envío es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            return envioDao.leer(id, connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener el envío", e, ServiceException.ErrorType.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Envio> getAll() throws ServiceException {
        try (Connection connection = dbConnection.getConnection()) {
            return envioDao.leerTodos(connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener los envíos", e, ServiceException.ErrorType.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean existe(Long id) throws ServiceException {
        if (id == null) {
            return false;
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            return envioDao.existe(id, connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al verificar la existencia del envío", e, ServiceException.ErrorType.DATABASE_ERROR);
        }
    }
    
    /**
     * Busca un envío por su número de tracking.
     * @param tracking número de tracking
     * @return envío encontrado o null si no existe
     * @throws ServiceException si ocurre un error en la operación
     */
    public Envio buscarPorTracking(String tracking) throws ServiceException {
        if (tracking == null || tracking.trim().isEmpty()) {
            throw new ServiceException(
                "El tracking es requerido para buscar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            EnvioDao envioDaoImpl = (EnvioDao) envioDao;
            return envioDaoImpl.buscarPorTracking(tracking.trim(), connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el envío por tracking", e, ServiceException.ErrorType.DATABASE_ERROR);
        }
    }
    
    /**
     * Valida los datos de un envío antes de persistir.
     * @param envio envío a validar
     * @throws ServiceException si la validación falla
     */
    private void validarEnvio(Envio envio) throws ServiceException {
        if (envio == null) {
            throw new ServiceException(
                "El envío no puede ser nulo",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio.getTracking() == null || envio.getTracking().trim().isEmpty()) {
            throw new ServiceException(
                "El tracking es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio.getTracking().length() > 40) {
            throw new ServiceException(
                "El tracking no puede tener más de 40 caracteres",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio.getEmpresa() == null) {
            throw new ServiceException(
                "La empresa es requerida",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio.getTipo() == null) {
            throw new ServiceException(
                "El tipo de envío es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio.getCosto() < 0) {
            throw new ServiceException(
                "El costo no puede ser negativo",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio.getEstado() == null) {
            envio.setEstado(Envio.EstadoEnvio.EN_PREPARACION);
        }
    }
}