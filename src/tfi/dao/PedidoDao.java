package tfi.dao;

import tfi.entities.Pedido;
import tfi.entities.Envio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO concreto para la entidad Pedido.
 * Implementa operaciones CRUD usando JDBC y PreparedStatement.
 */
public class PedidoDao implements GenericDao<Pedido> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO pedidos (numero, fecha, clienteNombre, total, estado, envio, eliminado) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT p.id, p.numero, p.fecha, p.clienteNombre, p.total, p.estado, p.envio, p.eliminado, " +
        "e.id as envio_id, e.tracking, e.empresa, e.tipo, e.costo, e.fechaDespacho, e.fechaEstimada, e.estado as envio_estado " +
        "FROM pedidos p " +
        "LEFT JOIN envios e ON p.envio = e.id AND e.eliminado = FALSE " +
        "WHERE p.id = ? AND p.eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT p.id, p.numero, p.fecha, p.clienteNombre, p.total, p.estado, p.envio, p.eliminado, " +
        "e.id as envio_id, e.tracking, e.empresa, e.tipo, e.costo, e.fechaDespacho, e.fechaEstimada, e.estado as envio_estado " +
        "FROM pedidos p " +
        "LEFT JOIN envios e ON p.envio = e.id AND e.eliminado = FALSE " +
        "WHERE p.eliminado = FALSE ORDER BY p.id";
    
    private static final String UPDATE_SQL = 
        "UPDATE pedidos SET numero = ?, fecha = ?, clienteNombre = ?, total = ?, " +
        "estado = ?, envio = ? WHERE id = ? AND eliminado = FALSE";
    
    private static final String DELETE_SQL = 
        "UPDATE pedidos SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    
    private static final String EXISTS_SQL = 
        "SELECT COUNT(*) FROM pedidos WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_BY_NUMERO_SQL = 
        "SELECT p.id, p.numero, p.fecha, p.clienteNombre, p.total, p.estado, p.envio, p.eliminado, " +
        "e.id as envio_id, e.tracking, e.empresa, e.tipo, e.costo, e.fechaDespacho, e.fechaEstimada, e.estado as envio_estado " +
        "FROM pedidos p " +
        "LEFT JOIN envios e ON p.envio = e.id AND e.eliminado = FALSE " +
        "WHERE p.numero = ? AND p.eliminado = FALSE";
    
    private static final String SELECT_BY_CLIENTE_SQL = 
        "SELECT p.id, p.numero, p.fecha, p.clienteNombre, p.total, p.estado, p.envio, p.eliminado, " +
        "e.id as envio_id, e.tracking, e.empresa, e.tipo, e.costo, e.fechaDespacho, e.fechaEstimada, e.estado as envio_estado " +
        "FROM pedidos p " +
        "LEFT JOIN envios e ON p.envio = e.id AND e.eliminado = FALSE " +
        "WHERE p.clienteNombre LIKE ? AND p.eliminado = FALSE ORDER BY p.fecha DESC";
    
    private static final String SELECT_BY_ENVIO_SQL = 
        "SELECT p.id, p.numero, p.fecha, p.clienteNombre, p.total, p.estado, p.envio, p.eliminado " +
        "FROM pedidos p WHERE p.envio = ? AND p.eliminado = FALSE";
    
    @Override
    public Pedido crear(Pedido pedido, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pedido.getNumero());
            stmt.setDate(2, Date.valueOf(pedido.getFecha()));
            stmt.setString(3, pedido.getClienteNombre());
            stmt.setDouble(4, pedido.getTotal());
            stmt.setString(5, pedido.getEstado().name());
            stmt.setObject(6, pedido.getEnvio() != null ? pedido.getEnvio().getId() : null, Types.BIGINT);
            stmt.setBoolean(7, pedido.isEliminado());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear el pedido");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pedido.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del pedido creado");
                }
            }
            
            return pedido;
        }
    }
    
    @Override
    public Pedido leer(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
                return null;
            }
        }
    }
    
    @Override
    public List<Pedido> leerTodos(Connection connection) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                pedidos.add(mapResultSetToPedido(rs));
            }
        }
        
        return pedidos;
    }
    
    @Override
    public Pedido actualizar(Pedido pedido, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, pedido.getNumero());
            stmt.setDate(2, Date.valueOf(pedido.getFecha()));
            stmt.setString(3, pedido.getClienteNombre());
            stmt.setDouble(4, pedido.getTotal());
            stmt.setString(5, pedido.getEstado().name());
            stmt.setObject(6, pedido.getEnvio() != null ? pedido.getEnvio().getId() : null, Types.BIGINT);
            stmt.setLong(7, pedido.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el pedido con ID: " + pedido.getId());
            }
            
            return pedido;
        }
    }
    
    @Override
    public boolean eliminar(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_SQL)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public boolean existe(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(EXISTS_SQL)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
     * Busca un pedido por su número.
     * @param numero número del pedido
     * @param connection conexión a la base de datos
     * @return pedido encontrado o null si no existe
     * @throws SQLException si ocurre un error en la base de datos
     */
    public Pedido buscarPorNumero(String numero, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_NUMERO_SQL)) {
            stmt.setString(1, numero);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
                return null;
            }
        }
    }
    
    /**
     * Busca pedidos por nombre de cliente.
     * @param clienteNombre nombre del cliente (puede contener wildcards)
     * @param connection conexión a la base de datos
     * @return lista de pedidos encontrados
     * @throws SQLException si ocurre un error en la base de datos
     */
    public List<Pedido> buscarPorCliente(String clienteNombre, Connection connection) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_CLIENTE_SQL)) {
            stmt.setString(1, "%" + clienteNombre + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        }
        
        return pedidos;
    }
    
    /**
     * Busca un pedido que tenga asociado un envío específico.
     * @param envioId ID del envío
     * @param connection conexión a la base de datos
     * @return pedido encontrado o null si no existe
     * @throws SQLException si ocurre un error en la base de datos
     */
    public Pedido buscarPorEnvio(Long envioId, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ENVIO_SQL)) {
            stmt.setLong(1, envioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
                return null;
            }
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Pedido.
     * @param rs ResultSet con los datos
     * @return objeto Pedido mapeado
     * @throws SQLException si ocurre un error al leer los datos
     */
    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getLong("id"));
        pedido.setNumero(rs.getString("numero"));
        pedido.setFecha(rs.getDate("fecha").toLocalDate());
        pedido.setClienteNombre(rs.getString("clienteNombre"));
        pedido.setTotal(rs.getDouble("total"));
        pedido.setEstado(Pedido.EstadoPedido.valueOf(rs.getString("estado")));
        pedido.setEliminado(rs.getBoolean("eliminado"));
        
        // Mapear envío si existe
        Long envioId = rs.getLong("envio");
        if (envioId > 0 && !rs.wasNull()) {
            Envio envio = new Envio();
            envio.setId(envioId);
            envio.setTracking(rs.getString("tracking"));
            if (rs.getString("empresa") != null) {
                envio.setEmpresa(Envio.EmpresaEnvio.valueOf(rs.getString("empresa")));
            }
            if (rs.getString("tipo") != null) {
                envio.setTipo(Envio.TipoEnvio.valueOf(rs.getString("tipo")));
            }
            envio.setCosto(rs.getDouble("costo"));
            
            Date fechaDespacho = rs.getDate("fechaDespacho");
            if (fechaDespacho != null) {
                envio.setFechaDespacho(fechaDespacho.toLocalDate());
            }
            
            Date fechaEstimada = rs.getDate("fechaEstimada");
            if (fechaEstimada != null) {
                envio.setFechaEstimada(fechaEstimada.toLocalDate());
            }
            
            if (rs.getString("envio_estado") != null) {
                envio.setEstado(Envio.EstadoEnvio.valueOf(rs.getString("envio_estado")));
            }
            
            pedido.setEnvio(envio);
        }
        
        return pedido;
    }
}
