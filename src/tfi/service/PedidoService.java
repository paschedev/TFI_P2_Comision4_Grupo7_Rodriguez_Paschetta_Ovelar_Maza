package tfi.service;

import tfi.config.DatabaseConnection;
import tfi.dao.PedidoDao;
import tfi.dao.EnvioDao;
import tfi.dao.GenericDao;
import tfi.entities.Pedido;
import tfi.entities.Envio;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para la entidad Pedido.
 * Implementa reglas de negocio y validaciones con manejo de transacciones robusto.
 * Incluye operaciones compuestas que involucran múltiples entidades.
 */
public class PedidoService implements GenericService<Pedido> {
    
    private final GenericDao<Pedido> pedidoDao;
    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    
    public PedidoService() {
        this.pedidoDao = new PedidoDao();
        this.dbConnection = DatabaseConnection.getInstance();
        this.transactionManager = new TransactionManager();
    }
    
    @Override
    public Pedido insertar(Pedido pedido) throws ServiceException {
        validarPedido(pedido);
        
        return transactionManager.executeWithRetry(connection -> {
            // Verificar que el número de pedido no exista
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido existente = pedidoDaoImpl.buscarPorNumero(pedido.getNumero(), connection);
            if (existente != null) {
                throw new ServiceException(
                    "Ya existe un pedido con el número: " + pedido.getNumero(),
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // Si tiene envío, verificar que no esté ya asociado a otro pedido
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
                Pedido pedidoConEnvio = pedidoDaoImpl.buscarPorEnvio(pedido.getEnvio().getId(), connection);
                if (pedidoConEnvio != null) {
                    throw new ServiceException(
                        "El envío con ID " + pedido.getEnvio().getId() + 
                        " ya está asociado al pedido " + pedidoConEnvio.getNumero(),
                        ServiceException.ErrorType.VALIDATION_ERROR
                    );
                }
            }
            
            // Si el pedido tiene envío asociado
            if (pedido.getEnvio() != null) {
                // Si el pedido ya tiene un envío, persistirlo primero
                Envio envioExistente = pedido.getEnvio();
                
                // Validar el envío antes de crearlo
                validarEnvio(envioExistente);
                
                // Crear el envío en la base de datos
                EnvioDao envioDao = new EnvioDao();
                Envio envioCreado = envioDao.crear(envioExistente, connection);
                
                // Asociar el envío creado al pedido
                pedido.setEnvio(envioCreado);
            }
            
            return pedidoDao.crear(pedido, connection);
        });
    }
    
    /**
     * Inserta un pedido con un envío específico.
     * @param pedido datos del pedido
     * @param envio datos del envío
     * @return pedido creado con el envío asociado
     * @throws ServiceException si ocurre un error en la operación
     */
    public Pedido insertarConEnvio(Pedido pedido, Envio envio) throws ServiceException {
        if (pedido == null) {
            throw new ServiceException(
                "Los datos del pedido son requeridos",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envio == null) {
            throw new ServiceException(
                "Los datos del envío son requeridos",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        // Asociar el envío al pedido antes de la validación
        pedido.setEnvio(envio);
        
        return insertar(pedido);
    }
    
    @Override
    public Pedido actualizar(Pedido pedido) throws ServiceException {
        validarPedido(pedido);
        
        if (pedido.getId() == null) {
            throw new ServiceException(
                "El ID del pedido es requerido para actualizar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // Verificar que el pedido existe
            if (!pedidoDao.existe(pedido.getId(), connection)) {
                throw new ServiceException(
                    "No existe un pedido con ID: " + pedido.getId(),
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // Verificar que el número no esté en uso por otro pedido
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido existente = pedidoDaoImpl.buscarPorNumero(pedido.getNumero(), connection);
            if (existente != null && !existente.getId().equals(pedido.getId())) {
                throw new ServiceException(
                    "Ya existe otro pedido con el número: " + pedido.getNumero(),
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // Si tiene envío, verificar que no esté ya asociado a otro pedido
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() != null) {
                Pedido pedidoConEnvio = pedidoDaoImpl.buscarPorEnvio(pedido.getEnvio().getId(), connection);
                if (pedidoConEnvio != null && !pedidoConEnvio.getId().equals(pedido.getId())) {
                    throw new ServiceException(
                        "El envío con ID " + pedido.getEnvio().getId() + 
                        " ya está asociado al pedido " + pedidoConEnvio.getNumero(),
                        ServiceException.ErrorType.VALIDATION_ERROR
                    );
                }
            }
            
            return pedidoDao.actualizar(pedido, connection);
        });
    }
    
    @Override
    public boolean eliminar(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException(
                "El ID del pedido es requerido para eliminar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // Verificar que el pedido existe
            if (!pedidoDao.existe(id, connection)) {
                throw new ServiceException(
                    "No existe un pedido con ID: " + id,
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            return pedidoDao.eliminar(id, connection);
        });
    }
    
    @Override
    public Pedido getById(Long id) throws ServiceException {
        if (id == null) {
            throw new ServiceException(
                "El ID del pedido es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            return pedidoDao.leer(id, connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener el pedido", e, ServiceException.ErrorType.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Pedido> getAll() throws ServiceException {
        try (Connection connection = dbConnection.getConnection()) {
            return pedidoDao.leerTodos(connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener los pedidos", e, ServiceException.ErrorType.DATABASE_ERROR);
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
            throw new ServiceException("Error al verificar la existencia del pedido", e, ServiceException.ErrorType.DATABASE_ERROR);
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
            throw new ServiceException(
                "El número del pedido es requerido para buscar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            return pedidoDaoImpl.buscarPorNumero(numero.trim(), connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el pedido por número", e, ServiceException.ErrorType.DATABASE_ERROR);
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
            throw new ServiceException(
                "El nombre del cliente es requerido para buscar",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        try (Connection connection = dbConnection.getConnection()) {
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            return pedidoDaoImpl.buscarPorCliente(clienteNombre.trim(), connection);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar pedidos por cliente", e, ServiceException.ErrorType.DATABASE_ERROR);
        }
    }
    
    /**
     * OPERACIÓN COMPUESTA: Crea un envío y lo asocia a un pedido en una sola transacción.
     * @param pedidoId ID del pedido
     * @param envioData datos del envío a crear
     * @return pedido actualizado con el envío asociado
     * @throws ServiceException si ocurre un error en la operación
     */
    public Pedido crearEnvioYAsociarAPedido(Long pedidoId, Envio envioData) throws ServiceException {
        if (pedidoId == null) {
            throw new ServiceException(
                "El ID del pedido es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envioData == null) {
            throw new ServiceException(
                "Los datos del envío son requeridos",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // 1. Verificar que el pedido existe
            Pedido pedido = pedidoDao.leer(pedidoId, connection);
            if (pedido == null) {
                throw new ServiceException(
                    "No existe un pedido con ID: " + pedidoId,
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // 2. Verificar que el pedido no tenga ya un envío asociado
            if (pedido.getEnvio() != null) {
                throw new ServiceException(
                    "El pedido " + pedido.getNumero() + " ya tiene un envío asociado",
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // 3. Validar datos del envío
            validarEnvio(envioData);
            
            // 4. Verificar que el tracking no exista
            if (envioData.getTracking() != null && !envioData.getTracking().trim().isEmpty()) {
                EnvioDao envioDao = new EnvioDao();
                Envio existente = envioDao.buscarPorTracking(envioData.getTracking(), connection);
                if (existente != null) {
                    throw new ServiceException(
                        "Ya existe un envío con el tracking: " + envioData.getTracking(),
                        ServiceException.ErrorType.VALIDATION_ERROR
                    );
                }
            }
            
            // 5. Crear el envío
            EnvioDao envioDao = new EnvioDao();
            Envio envioCreado = envioDao.crear(envioData, connection);
            
            // 6. Asociar el envío al pedido
            pedido.setEnvio(envioCreado);
            Pedido pedidoActualizado = pedidoDao.actualizar(pedido, connection);
            
            return pedidoActualizado;
        });
    }
    
    /**
     * OPERACIÓN COMPUESTA: Crea un pedido con un envío en una sola transacción.
     * @param pedidoData datos del pedido
     * @param envioData datos del envío
     * @return pedido creado con el envío asociado
     * @throws ServiceException si ocurre un error en la operación
     */
    public Pedido crearPedidoConEnvio(Pedido pedidoData, Envio envioData) throws ServiceException {
        if (pedidoData == null) {
            throw new ServiceException(
                "Los datos del pedido son requeridos",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (envioData == null) {
            throw new ServiceException(
                "Los datos del envío son requeridos",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // 1. Validar datos del pedido
            validarPedido(pedidoData);
            
            // 2. Validar datos del envío
            validarEnvio(envioData);
            
            // 3. Verificar que el número de pedido no exista
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido existente = pedidoDaoImpl.buscarPorNumero(pedidoData.getNumero(), connection);
            if (existente != null) {
                throw new ServiceException(
                    "Ya existe un pedido con el número: " + pedidoData.getNumero(),
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // 4. Verificar que el tracking no exista
            if (envioData.getTracking() != null && !envioData.getTracking().trim().isEmpty()) {
                EnvioDao envioDao = new EnvioDao();
                Envio envioExistente = envioDao.buscarPorTracking(envioData.getTracking(), connection);
                if (envioExistente != null) {
                    throw new ServiceException(
                        "Ya existe un envío con el tracking: " + envioData.getTracking(),
                        ServiceException.ErrorType.VALIDATION_ERROR
                    );
                }
            }
            
            // 5. Crear el envío primero
            EnvioDao envioDao = new EnvioDao();
            Envio envioCreado = envioDao.crear(envioData, connection);
            
            // 6. Asociar el envío al pedido
            pedidoData.setEnvio(envioCreado);
            
            // 7. Crear el pedido
            Pedido pedidoCreado = pedidoDao.crear(pedidoData, connection);
            
            return pedidoCreado;
        });
    }
    
    /**
     * Asocia un envío existente a un pedido.
     * @param pedidoId ID del pedido
     * @param envioId ID del envío
     * @return pedido actualizado
     * @throws ServiceException si ocurre un error en la operación
     */
    public Pedido asociarEnvio(Long pedidoId, Long envioId) throws ServiceException {
        if (pedidoId == null) {
            throw new ServiceException(
                "El ID del pedido es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        if (envioId == null) {
            throw new ServiceException(
                "El ID del envío es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        return transactionManager.executeWithRetry(connection -> {
            // 1. Verificar que el pedido existe
            Pedido pedido = pedidoDao.leer(pedidoId, connection);
            if (pedido == null) {
                throw new ServiceException(
                    "No existe un pedido con ID: " + pedidoId,
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // 2. Verificar que el envío existe
            EnvioDao envioDao = new EnvioDao();
            Envio envio = envioDao.leer(envioId, connection);
            if (envio == null) {
                throw new ServiceException(
                    "No existe un envío con ID: " + envioId,
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // 3. Verificar que el envío no esté ya asociado a otro pedido
            PedidoDao pedidoDaoImpl = (PedidoDao) pedidoDao;
            Pedido pedidoConEnvio = pedidoDaoImpl.buscarPorEnvio(envioId, connection);
            if (pedidoConEnvio != null) {
                throw new ServiceException(
                    "El envío con ID " + envioId + 
                    " ya está asociado al pedido " + pedidoConEnvio.getNumero(),
                    ServiceException.ErrorType.VALIDATION_ERROR
                );
            }
            
            // 4. Asociar el envío al pedido
            pedido.setEnvio(envio);
            Pedido resultado = pedidoDao.actualizar(pedido, connection);
            
            return resultado;
        });
    }
    
    
    /**
     * Valida los datos de un pedido antes de persistir.
     * @param pedido pedido a validar
     * @throws ServiceException si la validación falla
     */
    private void validarPedido(Pedido pedido) throws ServiceException {
        if (pedido == null) {
            throw new ServiceException(
                "El pedido no puede ser nulo",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getNumero() == null || pedido.getNumero().trim().isEmpty()) {
            throw new ServiceException(
                "El número del pedido es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getNumero().length() > 20) {
            throw new ServiceException(
                "El número del pedido no puede tener más de 20 caracteres",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getFecha() == null) {
            throw new ServiceException(
                "La fecha del pedido es requerida",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getClienteNombre() == null || pedido.getClienteNombre().trim().isEmpty()) {
            throw new ServiceException(
                "El nombre del cliente es requerido",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getClienteNombre().length() > 120) {
            throw new ServiceException(
                "El nombre del cliente no puede tener más de 120 caracteres",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getTotal() < 0) {
            throw new ServiceException(
                "El total del pedido no puede ser negativo",
                ServiceException.ErrorType.VALIDATION_ERROR
            );
        }
        
        if (pedido.getEstado() == null) {
            pedido.setEstado(Pedido.EstadoPedido.NUEVO);
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