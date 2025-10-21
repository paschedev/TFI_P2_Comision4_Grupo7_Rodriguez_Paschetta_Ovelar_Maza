package tfi.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica para operaciones CRUD en la base de datos.
 * @param <T> tipo de entidad
 */
public interface GenericDao<T> {
    
    /**
     * Crea una nueva entidad en la base de datos.
     * @param entity entidad a crear
     * @param connection conexión a la base de datos
     * @return entidad creada con ID asignado
     * @throws SQLException si ocurre un error en la base de datos
     */
    T crear(T entity, Connection connection) throws SQLException;
    
    /**
     * Lee una entidad por su ID.
     * @param id ID de la entidad
     * @param connection conexión a la base de datos
     * @return entidad encontrada o null si no existe
     * @throws SQLException si ocurre un error en la base de datos
     */
    T leer(Long id, Connection connection) throws SQLException;
    
    /**
     * Lee todas las entidades no eliminadas.
     * @param connection conexión a la base de datos
     * @return lista de entidades
     * @throws SQLException si ocurre un error en la base de datos
     */
    List<T> leerTodos(Connection connection) throws SQLException;
    
    /**
     * Actualiza una entidad existente.
     * @param entity entidad a actualizar
     * @param connection conexión a la base de datos
     * @return entidad actualizada
     * @throws SQLException si ocurre un error en la base de datos
     */
    T actualizar(T entity, Connection connection) throws SQLException;
    
    /**
     * Elimina lógicamente una entidad por su ID.
     * @param id ID de la entidad a eliminar
     * @param connection conexión a la base de datos
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws SQLException si ocurre un error en la base de datos
     */
    boolean eliminar(Long id, Connection connection) throws SQLException;
    
    /**
     * Verifica si una entidad existe por su ID.
     * @param id ID de la entidad
     * @param connection conexión a la base de datos
     * @return true si existe, false en caso contrario
     * @throws SQLException si ocurre un error en la base de datos
     */
    boolean existe(Long id, Connection connection) throws SQLException;
}

