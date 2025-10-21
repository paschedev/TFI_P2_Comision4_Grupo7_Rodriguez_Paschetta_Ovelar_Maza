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
 * Implementa reglas de negocio y validaciones.
 */
public class EnvioService implements GenericService<Envio> {
    
    private final GenericDao<Envio> envioDao;
    private final DatabaseConnection dbConnection;
    
    public EnvioService() {
        this.envioDao = new EnvioDao();
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    @Override
    public Envio insertar(Envio envio) throws ServiceException {
        validarEnvio(envio);
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el tracking no exista
            if (envio.getTracking() != null && !envio.getTracking().trim().isEmpty()) {
                EnvioDao envioDaoImpl = (EnvioDao) envioDao;
                Envio existente = envioDaoImpl.buscarPorTracking(envio.getTracking(), connection);
                if (existente != null) {
                    throw new ServiceException("Ya existe un envío con el tracking: " + envio.getTracking());
                }
            }
            
            Envio resultado = envioDao.crear(envio, connection);
            connection.commit();
            
            return resultado;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new ServiceException("Error al hacer rollback", rollbackEx);
                }
            }
            throw new ServiceException("Error al insertar el envío", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw new ServiceException("Error al cerrar la conexión", e);
                }
            }
        }
    }
    
    @Override
    public Envio actualizar(Envio envio) throws ServiceException {
        validarEnvio(envio);
        
        if (envio.getId() == null) {
            throw new ServiceException("El ID del envío es requerido para actualizar");
        }
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el envío existe
            if (!envioDao.existe(envio.getId(), connection)) {
                throw new ServiceException("No existe un envío con ID: " + envio.getId());
            }
            
            // Verificar que el tracking no esté en uso por otro envío
            if (envio.getTracking() != null && !envio.getTracking().trim().isEmpty()) {
                EnvioDao envioDaoImpl = (EnvioDao) envioDao;
                Envio existente = envioDaoImpl.buscarPorTracking(envio.getTracking(), connection);
                if (existente != null && !existente.getId().equals(envio.getId())) {
                    throw new ServiceException("Ya existe otro envío con el tracking: " + envio.getTracking());
                }
            }
            
            Envio resultado = envioDao.actualizar(envio, connection);
            connection.commit();
            
            return resultado;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new ServiceException("Error al hacer rollback", rollbackEx);
                }
            }
            throw new ServiceException("Error al actualizar el envío", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw new ServiceException("Error al cerrar la conexión", e);
                }
            }
        }
    }
    
    @Override
    public boolean eliminar(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("El ID del envío es requerido para eliminar");
        }
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el envío existe
            if (!envioDao.existe(id, connection)) {
                throw new ServiceException("No existe un envío con ID: " + id);
            }
            
            boolean resultado = envioDao.eliminar(id, connection);
            connection.commit();
            
            return resultado;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw new ServiceException("Error al hacer rollback", rollbackEx);
                }
            }
            throw new ServiceException("Error al eliminar el envío", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw new ServiceException("Error al cerrar la conexión", e);
                }
            }
        }
    }
    
    @Override
    public Envio getById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("El ID del envío es requerido");
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            return envioDao.leer(id, connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener el envío", e);
        }
    }
    
    @Override
    public List<Envio> getAll() throws ServiceException {
        try (Connection connection = dbConnection.getConnection()) {
            return envioDao.leerTodos(connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener los envíos", e);
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
            throw new ServiceException("Error al verificar la existencia del envío", e);
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
            throw new ServiceException("El tracking es requerido para buscar");
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            EnvioDao envioDaoImpl = (EnvioDao) envioDao;
            return envioDaoImpl.buscarPorTracking(tracking.trim(), connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el envío por tracking", e);
        }
    }
    
    /**
     * Valida los datos de un envío antes de persistir.
     * @param envio envío a validar
     * @throws ServiceException si la validación falla
     */
    private void validarEnvio(Envio envio) throws ServiceException {
        if (envio == null) {
            throw new ServiceException("El envío no puede ser nulo");
        }
        
        if (envio.getTracking() == null || envio.getTracking().trim().isEmpty()) {
            throw new ServiceException("El tracking es requerido");
        }
        
        if (envio.getTracking().length() > 40) {
            throw new ServiceException("El tracking no puede tener más de 40 caracteres");
        }
        
        if (envio.getEmpresa() == null) {
            throw new ServiceException("La empresa es requerida");
        }
        
        if (envio.getTipo() == null) {
            throw new ServiceException("El tipo de envío es requerido");
        }
        
        if (envio.getCosto() <= 0) {
            throw new ServiceException("El costo debe ser mayor a 0");
        }
        
        if (envio.getEstado() == null) {
            envio.setEstado(Envio.EstadoEnvio.EN_PREPARACION);
        }
    }
}
