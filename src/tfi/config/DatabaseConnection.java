package tfi.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase para manejar la conexión a la base de datos MySQL.
 * Utiliza un patrón Singleton para garantizar una única instancia.
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Properties properties;
    private String url;
    private String username;
    private String password;
    private String driver;
    
    private DatabaseConnection() {
        loadProperties();
    }
    
    /**
     * Obtiene la instancia única de DatabaseConnection.
     * @return instancia de DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Carga las propiedades de configuración desde el archivo database.properties.
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("No se pudo encontrar el archivo database.properties");
            }
            properties.load(input);
            
            this.driver = properties.getProperty("db.driver");
            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
            
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las propiedades de la base de datos", e);
        }
    }
    
    /**
     * Obtiene una nueva conexión a la base de datos.
     * @return Connection a la base de datos
     * @throws SQLException si ocurre un error al conectar
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver de base de datos no encontrado: " + driver, e);
        }
    }
    
    /**
     * Prueba la conexión a la base de datos.
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error al probar la conexión: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene la URL de la base de datos.
     * @return URL de la base de datos
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Obtiene el nombre de usuario de la base de datos.
     * @return nombre de usuario
     */
    public String getUsername() {
        return username;
    }
}
