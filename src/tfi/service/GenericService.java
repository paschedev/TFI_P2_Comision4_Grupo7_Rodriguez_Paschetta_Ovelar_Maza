package tfi.service;

import java.util.List;

/**
 * Interfaz genérica para servicios de negocio.
 * Define operaciones comunes para todas las entidades.
 * @param <T> tipo de entidad
 */
public interface GenericService<T> {
    
    /**
     * Inserta una nueva entidad.
     * @param entity entidad a insertar
     * @return entidad insertada con ID asignado
     * @throws ServiceException si ocurre un error en la operación
     */
    T insertar(T entity) throws ServiceException;
    
    /**
     * Actualiza una entidad existente.
     * @param entity entidad a actualizar
     * @return entidad actualizada
     * @throws ServiceException si ocurre un error en la operación
     */
    T actualizar(T entity) throws ServiceException;
    
    /**
     * Elimina lógicamente una entidad por su ID.
     * @param id ID de la entidad a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws ServiceException si ocurre un error en la operación
     */
    boolean eliminar(Long id) throws ServiceException;
    
    /**
     * Obtiene una entidad por su ID.
     * @param id ID de la entidad
     * @return entidad encontrada o null si no existe
     * @throws ServiceException si ocurre un error en la operación
     */
    T getById(Long id) throws ServiceException;
    
    /**
     * Obtiene todas las entidades no eliminadas.
     * @return lista de entidades
     * @throws ServiceException si ocurre un error en la operación
     */
    List<T> getAll() throws ServiceException;
    
    /**
     * Verifica si una entidad existe por su ID.
     * @param id ID de la entidad
     * @return true si existe, false en caso contrario
     * @throws ServiceException si ocurre un error en la operación
     */
    boolean existe(Long id) throws ServiceException;
}
