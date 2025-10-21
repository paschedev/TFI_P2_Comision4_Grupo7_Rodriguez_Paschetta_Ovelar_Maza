package tfi.main;

import tfi.config.DatabaseConnection;

/**
 * Clase principal de la aplicación.
 * Punto de entrada del sistema de gestión de pedidos y envíos.
 */
public class Main {
    
    public static void main(String[] args) {
        try {
            // Verificar conexión a la base de datos
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            if (!dbConnection.testConnection()) {
                System.err.println("Error: No se pudo conectar a la base de datos.");
                System.err.println("Verifique que:");
                System.err.println("1. MySQL esté ejecutándose");
                System.err.println("2. La base de datos 'tfi_pedidos' exista");
                System.err.println("3. Las credenciales en database.properties sean correctas");
                System.err.println("4. El driver de MySQL esté en el classpath");
                System.exit(1);
            }
            
            System.out.println("Conexión a la base de datos establecida correctamente.");
            
            // Iniciar el menú de la aplicación
            AppMenu appMenu = new AppMenu();
            appMenu.iniciar();
            
        } catch (Exception e) {
            System.err.println("Error fatal al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
