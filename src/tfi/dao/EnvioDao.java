package tfi.dao;

import tfi.entities.Envio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO concreto para la entidad Envio.
 * Implementa operaciones CRUD usando JDBC y PreparedStatement.
 */
public class EnvioDao implements GenericDao<Envio> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO envios (tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estado, eliminado) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT id, tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estado, eliminado " +
        "FROM envios WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT id, tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estado, eliminado " +
        "FROM envios WHERE eliminado = FALSE ORDER BY id";
    
    private static final String UPDATE_SQL = 
        "UPDATE envios SET tracking = ?, empresa = ?, tipo = ?, costo = ?, " +
        "fechaDespacho = ?, fechaEstimada = ?, estado = ? WHERE id = ? AND eliminado = FALSE";
    
    private static final String DELETE_SQL = 
        "UPDATE envios SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    
    private static final String EXISTS_SQL = 
        "SELECT COUNT(*) FROM envios WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_BY_TRACKING_SQL = 
        "SELECT id, tracking, empresa, tipo, costo, fechaDespacho, fechaEstimada, estado, eliminado " +
        "FROM envios WHERE tracking = ? AND eliminado = FALSE";
    
    @Override
    public Envio crear(Envio envio, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, envio.getTracking());
            stmt.setString(2, envio.getEmpresa().name());
            stmt.setString(3, envio.getTipo().name());
            stmt.setDouble(4, envio.getCosto());
            stmt.setDate(5, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
            stmt.setDate(6, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
            stmt.setString(7, envio.getEstado().name());
            stmt.setBoolean(8, envio.isEliminado());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear el envío");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    envio.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del envío creado");
                }
            }
            
            return envio;
        }
    }
    
    @Override
    public Envio leer(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnvio(rs);
                }
                return null;
            }
        }
    }
    
    @Override
    public List<Envio> leerTodos(Connection connection) throws SQLException {
        List<Envio> envios = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                envios.add(mapResultSetToEnvio(rs));
            }
        }
        
        return envios;
    }
    
    @Override
    public Envio actualizar(Envio envio, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, envio.getTracking());
            stmt.setString(2, envio.getEmpresa().name());
            stmt.setString(3, envio.getTipo().name());
            stmt.setDouble(4, envio.getCosto());
            stmt.setDate(5, envio.getFechaDespacho() != null ? Date.valueOf(envio.getFechaDespacho()) : null);
            stmt.setDate(6, envio.getFechaEstimada() != null ? Date.valueOf(envio.getFechaEstimada()) : null);
            stmt.setString(7, envio.getEstado().name());
            stmt.setLong(8, envio.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el envío con ID: " + envio.getId());
            }
            
            return envio;
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
     * Busca un envío por su número de tracking.
     * @param tracking número de tracking
     * @param connection conexión a la base de datos
     * @return envío encontrado o null si no existe
     * @throws SQLException si ocurre un error en la base de datos
     */
    public Envio buscarPorTracking(String tracking, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_TRACKING_SQL)) {
            stmt.setString(1, tracking);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnvio(rs);
                }
                return null;
            }
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Envio.
     * @param rs ResultSet con los datos
     * @return objeto Envio mapeado
     * @throws SQLException si ocurre un error al leer los datos
     */
    private Envio mapResultSetToEnvio(ResultSet rs) throws SQLException {
        Envio envio = new Envio();
        envio.setId(rs.getLong("id"));
        envio.setTracking(rs.getString("tracking"));
        envio.setEmpresa(Envio.EmpresaEnvio.valueOf(rs.getString("empresa")));
        envio.setTipo(Envio.TipoEnvio.valueOf(rs.getString("tipo")));
        envio.setCosto(rs.getDouble("costo"));
        
        Date fechaDespacho = rs.getDate("fechaDespacho");
        if (fechaDespacho != null) {
            envio.setFechaDespacho(fechaDespacho.toLocalDate());
        }
        
        Date fechaEstimada = rs.getDate("fechaEstimada");
        if (fechaEstimada != null) {
            envio.setFechaEstimada(fechaEstimada.toLocalDate());
        }
        
        envio.setEstado(Envio.EstadoEnvio.valueOf(rs.getString("estado")));
        envio.setEliminado(rs.getBoolean("eliminado"));
        
        return envio;
    }
}
