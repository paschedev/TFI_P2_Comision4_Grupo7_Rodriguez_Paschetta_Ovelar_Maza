package tfi.service;

import tfi.config.DatabaseConnection;
import tfi.dao.PedidoDao;
import tfi.dao.GenericDao;
import tfi.entities.Pedido;
import tfi.entities.Envio;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para la entidad Pedido.
 * Implementa reglas de negocio y validaciones.
 */
public class PedidoService implements GenericService<Pedido> {
    
    private final GenericDao<Pedido> pedidoDao;
    private final DatabaseConnection dbConnection;
    
    public PedidoService() {
        this.pedidoDao = new PedidoDao();
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    @Override
    public Pedido insertar(Pedido pedido) throws ServiceException {
        validarPedido(pedido);
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el número de pedido no exista
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido existente = pedidoDaoImpl.buscarPorNumero(pedido.getNumero(), connection);
            if (existente != null) {
                throw new ServiceException("Ya existe un pedido con el número: " + pedido.getNumero());
            }
            
            // Si tiene envío, verificar que no esté ya asociado a otro pedido
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
                Pedido pedidoConEnvio = pedidoDaoImpl.buscarPorEnvio(pedido.getEnvio().getId(), connection);
                if (pedidoConEnvio != null) {
                    throw new ServiceException("El envío con ID " + pedido.getEnvio().getId() + 
                                            " ya está asociado al pedido " + pedidoConEnvio.getNumero());
                }
            }
            
            Pedido resultado = pedidoDao.crear(pedido, connection);
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
            throw new ServiceException("Error al insertar el pedido", e);
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
    public Pedido actualizar(Pedido pedido) throws ServiceException {
        validarPedido(pedido);
        
        if (pedido.getId() == null) {
            throw new ServiceException("El ID del pedido es requerido para actualizar");
        }
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el pedido existe
            if (!pedidoDao.existe(pedido.getId(), connection)) {
                throw new ServiceException("No existe un pedido con ID: " + pedido.getId());
            }
            
            // Verificar que el número no esté en uso por otro pedido
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido existente = pedidoDaoImpl.buscarPorNumero(pedido.getNumero(), connection);
            if (existente != null && !existente.getId().equals(pedido.getId())) {
                throw new ServiceException("Ya existe otro pedido con el número: " + pedido.getNumero());
            }
            
            // Si tiene envío, verificar que no esté ya asociado a otro pedido
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
                Pedido pedidoConEnvio = pedidoDaoImpl.buscarPorEnvio(pedido.getEnvio().getId(), connection);
                if (pedidoConEnvio != null && !pedidoConEnvio.getId().equals(pedido.getId())) {
                    throw new ServiceException("El envío con ID " + pedido.getEnvio().getId() + 
                                            " ya está asociado al pedido " + pedidoConEnvio.getNumero());
                }
            }
            
            Pedido resultado = pedidoDao.actualizar(pedido, connection);
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
            throw new ServiceException("Error al actualizar el pedido", e);
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
            throw new ServiceException("El ID del pedido es requerido para eliminar");
        }
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el pedido existe
            if (!pedidoDao.existe(id, connection)) {
                throw new ServiceException("No existe un pedido con ID: " + id);
            }
            
            boolean resultado = pedidoDao.eliminar(id, connection);
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
            throw new ServiceException("Error al eliminar el pedido", e);
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
    public Pedido getById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException("El ID del pedido es requerido");
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            return pedidoDao.leer(id, connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener el pedido", e);
        }
    }
    
    @Override
    public List<Pedido> getAll() throws ServiceException {
        try (Connection connection = dbConnection.getConnection()) {
            return pedidoDao.leerTodos(connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener los pedidos", e);
        }
    }
    
    @Override
    public boolean existe(Long id) throws ServiceException {
        if (id == null) {
            return false;
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            return pedidoDao.existe(id, connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al verificar la existencia del pedido", e);
        }
    }
    
    /**
     * Busca un pedido por su número.
     * @param numero número del pedido
     * @return pedido encontrado o null si no existe
     * @throws ServiceException si ocurre un error en la operación
     */
    public Pedido buscarPorNumero(String numero) throws ServiceException {
        if (numero == null || numero.trim().isEmpty()) {
            throw new ServiceException("El número del pedido es requerido para buscar");
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            return pedidoDaoImpl.buscarPorNumero(numero.trim(), connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el pedido por número", e);
        }
    }
    
    /**
     * Busca pedidos por nombre de cliente.
     * @param clienteNombre nombre del cliente
     * @return lista de pedidos encontrados
     * @throws ServiceException si ocurre un error en la operación
     */
    public List<Pedido> buscarPorCliente(String clienteNombre) throws ServiceException {
        if (clienteNombre == null || clienteNombre.trim().isEmpty()) {
            throw new ServiceException("El nombre del cliente es requerido para buscar");
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            return pedidoDaoImpl.buscarPorCliente(clienteNombre.trim(), connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar pedidos por cliente", e);
        }
    }
    
    /**
     * Asocia un envío a un pedido.
     * @param pedidoId ID del pedido
     * @param envioId ID del envío
     * @return pedido actualizado
     * @throws ServiceException si ocurre un error en la operación
     */
    public Pedido asociarEnvio(Long pedidoId, Long envioId) throws ServiceException {
        if (pedidoId == null) {
            throw new ServiceException("El ID del pedido es requerido");
        }
        if (envioId == null) {
            throw new ServiceException("El ID del envío es requerido");
        }
        
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            
            // Verificar que el pedido existe
            Pedido pedido = pedidoDao.leer(pedidoId, connection);
            if (pedido == null) {
                throw new ServiceException("No existe un pedido con ID: " + pedidoId);
            }
            
            // Verificar que el envío existe
            EnvioService envioService = new EnvioService();
            Envio envio = envioService.getById(envioId);
            if (envio == null) {
                throw new ServiceException("No existe un envío con ID: " + envioId);
            }
            
            // Verificar que el envío no esté ya asociado a otro pedido
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido pedidoConEnvio = pedidoDaoImpl.buscarPorEnvio(envioId, connection);
            if (pedidoConEnvio != null) {
                throw new ServiceException("El envío con ID " + envioId + 
                                        " ya está asociado al pedido " + pedidoConEnvio.getNumero());
            }
            
            // Asociar el envío al pedido
            pedido.setEnvio(envio);
            Pedido resultado = pedidoDao.actualizar(pedido, connection);
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
            throw new ServiceException("Error al asociar el envío al pedido", e);
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
    
    /**
     * Valida los datos de un pedido antes de persistir.
     * @param pedido pedido a validar
     * @throws ServiceException si la validación falla
     */
    private void validarPedido(Pedido pedido) throws ServiceException {
        if (pedido == null) {
            throw new ServiceException("El pedido no puede ser nulo");
        }
        
        if (pedido.getNumero() == null || pedido.getNumero().trim().isEmpty()) {
            throw new ServiceException("El número del pedido es requerido");
        }
        
        if (pedido.getNumero().length() > 20) {
            throw new ServiceException("El número del pedido no puede tener más de 20 caracteres");
        }
        
        if (pedido.getFecha() == null) {
            throw new ServiceException("La fecha del pedido es requerida");
        }
        
        if (pedido.getClienteNombre() == null || pedido.getClienteNombre().trim().isEmpty()) {
            throw new ServiceException("El nombre del cliente es requerido");
        }
        
        if (pedido.getClienteNombre().length() > 120) {
            throw new ServiceException("El nombre del cliente no puede tener más de 120 caracteres");
        }
        
        if (pedido.getTotal() <= 0) {
            throw new ServiceException("El total del pedido debe ser mayor a 0"); 
        }
        
        if (pedido.getEstado() == null) {
            pedido.setEstado(Pedido.EstadoPedido.NUEVO);
        }
    }
}
