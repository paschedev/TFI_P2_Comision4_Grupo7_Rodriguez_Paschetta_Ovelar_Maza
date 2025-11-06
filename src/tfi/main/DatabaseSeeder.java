package tfi.main;

import tfi.config.DatabaseConnection;
import tfi.dao.EnvioDao;
import tfi.dao.PedidoDao;
import tfi.entities.Envio;
import tfi.entities.Pedido;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeder para poblar la base de datos con datos de prueba.
 * Genera 50,000 envíos y 50,000 pedidos asociados sin usar tablas intermedias.
 * Basado en la lógica del archivo seeder.sql pero implementado directamente en Java.
 */
public class DatabaseSeeder {
    
    private static final int TOTAL_REGISTROS = 500;
    private static final LocalDate FECHA_BASE = LocalDate.of(2025, 10, 1);
    
    // Datos para generar combinaciones
    private static final String[] NOMBRES = {
        "Ana Pérez", "Luis Gómez", "María Díaz", "Juan Ruiz", "Sofía Martín", "Hector Sanchez", 
        "Martín Torres", "Lucía Paz", "Carla López", "Diego Ramos", "Tomás Vega", "Marcelo Pinola"
    };
    
    private static final Envio.EmpresaEnvio[] EMPRESAS = {
        Envio.EmpresaEnvio.ANDREANI,
        Envio.EmpresaEnvio.OCA,
        Envio.EmpresaEnvio.CORREO_ARG
    };
    
    private static final Envio.TipoEnvio[] TIPOS = {
        Envio.TipoEnvio.ESTANDAR,
        Envio.TipoEnvio.EXPRES
    };
    
    private static final Envio.EstadoEnvio[] ESTADOS_ENVIO = {
        Envio.EstadoEnvio.EN_PREPARACION,
        Envio.EstadoEnvio.EN_TRANSITO,
        Envio.EstadoEnvio.ENTREGADO
    };
    
    private final DatabaseConnection dbConnection;
    private final EnvioDao envioDao;
    private final PedidoDao pedidoDao;
    private final Random random;
    
    public DatabaseSeeder() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.envioDao = new EnvioDao();
        this.pedidoDao = new PedidoDao();
        this.random = new Random();
    }
    
    /**
     * Ejecuta el seeder para poblar la base de datos.
     * @throws SQLException si ocurre un error en la base de datos
     */
    public void seed() throws SQLException {
        System.out.println("==========================================");
        System.out.println("Iniciando seeder...");
        System.out.println("TOTAL_REGISTROS configurado: " + TOTAL_REGISTROS);
        System.out.println("Generando " + TOTAL_REGISTROS + " envíos y " + TOTAL_REGISTROS + " pedidos (total: " + (TOTAL_REGISTROS * 2) + " registros)...");
        System.out.println("==========================================");
        
        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Generar envíos de forma aleatoria (más realista)
                List<Envio> envios = generarEnvios();
                    
                // Insertar envíos en la base de datos
                System.out.println("Insertando envíos en la base de datos...");
                insertarEnvios(envios, connection);
                System.out.println("Envíos insertados correctamente");
                
                // Generar pedidos asociados a los envíos
                System.out.println("Generando pedidos...");
                List<Pedido> pedidos = generarPedidos(envios);
                                
                // Insertar pedidos en la base de datos
                System.out.println("Insertando pedidos en la base de datos...");
                insertarPedidos(pedidos, connection);
                System.out.println("Pedidos insertados correctamente");
                
                connection.commit();
                System.out.println("Seeder completado exitosamente!");
                
                // Mostrar estadísticas
                mostrarEstadisticas(connection);
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Genera los envíos de forma aleatoria para simular datos reales.
     * Las fechas se generan secuencialmente desde FECHA_BASE hasta la fecha actual.
     */
    private List<Envio> generarEnvios() {
        List<Envio> envios = new ArrayList<>();
        int rn = 0;
        
        // Calcular el rango de días entre la fecha base y la fecha actual
        LocalDate fechaActual = LocalDate.now();
        long diasHastaActual = ChronoUnit.DAYS.between(FECHA_BASE, fechaActual);
        
        // Asegurar que haya al menos algunos días de diferencia
        if (diasHastaActual < 0) {
            // Si la fecha actual es anterior a FECHA_BASE, usar solo FECHA_BASE
            diasHastaActual = 0;
        }
        
        // Calcular el intervalo de días entre registros para distribución secuencial
        // Si hay solo 1 registro, todos tendrán la misma fecha (FECHA_BASE)
        double intervaloDias = (diasHastaActual > 0 && TOTAL_REGISTROS > 1) 
            ? (double) diasHastaActual / (TOTAL_REGISTROS - 1) 
            : 0;
        
        for (int i = 0; i < TOTAL_REGISTROS; i++) {
            // Seleccionar aleatoriamente empresa, tipo y estado
            Envio.EmpresaEnvio empresa = EMPRESAS[random.nextInt(EMPRESAS.length)];
            Envio.TipoEnvio tipo = TIPOS[random.nextInt(TIPOS.length)];
            
            Envio.EstadoEnvio estado = ESTADOS_ENVIO[random.nextInt(ESTADOS_ENVIO.length)];
            
            // Calcular días secuenciales basado en el índice
            long diasSecuenciales = Math.round(i * intervaloDias);
            
            Envio envio = new Envio();
            envio.setTracking(String.format("TRK%010d", ++rn));
            envio.setEmpresa(empresa);
            envio.setTipo(tipo);
            
            // Costo aleatorio entre 15 y 100 (con o sin decimales)
            double costo = 15 + random.nextDouble() * (100 - 15);
            // Redondear a 2 decimales, pero permitir que algunas veces sea entero
            if (random.nextBoolean()) {
                // A veces sin decimales (entero)
                envio.setCosto(Math.round(costo));
            } else {
                // Otras veces con decimales
                envio.setCosto(Math.round(costo * 100.0) / 100.0);
            }
            
            // Fecha de despacho: secuencial desde FECHA_BASE hasta la fecha actual
            LocalDate fechaDespacho = FECHA_BASE.plusDays(diasSecuenciales);
            // Asegurar que no exceda la fecha actual
            if (fechaDespacho.isAfter(fechaActual)) {
                fechaDespacho = fechaActual;
            }
            
            envio.setFechaDespacho(fechaDespacho);
            // Fecha estimada: entre 3 y 10 días después del despacho
            envio.setFechaEstimada(fechaDespacho.plusDays(3 + random.nextInt(7)));
            envio.setEstado(estado);
            envio.setEliminado(false);
            
            envios.add(envio);
        }
        
        // Ordenar envíos por fecha de despacho para asegurar orden secuencial
        envios.sort((e1, e2) -> {
            if (e1.getFechaDespacho() == null && e2.getFechaDespacho() == null) return 0;
            if (e1.getFechaDespacho() == null) return 1;
            if (e2.getFechaDespacho() == null) return -1;
            return e1.getFechaDespacho().compareTo(e2.getFechaDespacho());
        });
        
        return envios;
    }
    
    /**
     * Genera los pedidos asociados a los envíos.
     * Las fechas de los pedidos se generan secuencialmente para mantener el orden cronológico.
     */
    private List<Pedido> generarPedidos(List<Envio> envios) {
        List<Pedido> pedidos = new ArrayList<>();
        LocalDate fechaPedidoAnterior = null;
        
        for (int i = 0; i < envios.size(); i++) {
            Envio envio = envios.get(i);
            int rn = i + 1; // Número secuencial (1..TOTAL_REGISTROS)
            
            Pedido pedido = new Pedido();
            pedido.setNumero(String.format("PED%010d", rn));
            
            // Calcular fecha del pedido de forma secuencial
            // El pedido debe ser anterior a la fecha de despacho (entre 1 y 10 días antes)
            LocalDate fechaDespacho = envio.getFechaDespacho();
            int diasAntes = 1 + random.nextInt(10);
            LocalDate fechaPedido = fechaDespacho.minusDays(diasAntes);
            
            // Asegurar que la fecha del pedido sea secuencial (no menor que el pedido anterior)
            if (fechaPedidoAnterior != null && fechaPedido.isBefore(fechaPedidoAnterior)) {
                // Si la fecha calculada es anterior al pedido anterior, usar la fecha del pedido anterior + 1 día
                // pero asegurando que no exceda la fecha de despacho
                fechaPedido = fechaPedidoAnterior.plusDays(1);
                if (fechaPedido.isAfter(fechaDespacho) || fechaPedido.isEqual(fechaDespacho)) {
                    // Si no puede ser después del anterior sin exceder el despacho, usar 1 día antes del despacho
                    fechaPedido = fechaDespacho.minusDays(1);
                }
            }
            
            pedido.setFecha(fechaPedido);
            fechaPedidoAnterior = fechaPedido; // Guardar para el próximo pedido
            
            // Cliente aleatorio entre los nombres disponibles
            pedido.setClienteNombre(NOMBRES[random.nextInt(NOMBRES.length)]);
            
            // Total aleatorio entre 100 y 750 (con o sin decimales)
            double total = 100 + random.nextDouble() * (750 - 100);
            // Redondear a 2 decimales, pero permitir que algunas veces sea entero
            if (random.nextBoolean()) {
                // A veces sin decimales (entero)
                pedido.setTotal(Math.round(total));
            } else {
                // Otras veces con decimales
                pedido.setTotal(Math.round(total * 100.0) / 100.0);
            }
            
            // Regla de negocio: Estado del pedido basado en el estado del envío
            // Si el envío está ENTREGADO -> el pedido es ENVIADO
            // Si el envío NO está ENTREGADO -> el pedido es FACTURADO
            if (envio.getEstado() == Envio.EstadoEnvio.ENTREGADO) {
                pedido.setEstado(Pedido.EstadoPedido.ENVIADO);
            } else {
                pedido.setEstado(Pedido.EstadoPedido.FACTURADO);
            }
            
            pedido.setEnvio(envio);
            pedido.setEliminado(false);
            
            pedidos.add(pedido);
        }
        
        // Ordenar pedidos por fecha para asegurar orden secuencial
        pedidos.sort((p1, p2) -> {
            if (p1.getFecha() == null && p2.getFecha() == null) return 0;
            if (p1.getFecha() == null) return 1;
            if (p2.getFecha() == null) return -1;
            return p1.getFecha().compareTo(p2.getFecha());
        });
        
        return pedidos;
    }
    
    /**
     * Inserta los envíos en la base de datos usando batch inserts para mejor rendimiento.
     */
    private void insertarEnvios(List<Envio> envios, Connection connection) throws SQLException {
        int batchSize = 1000;
        int total = envios.size();
        int registrosInsertados = 0;
        
        System.out.println("Insertando " + total + " envíos en la base de datos...");
        
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            
            // Insertar registros del batch actual
            for (int j = i; j < end; j++) {
                envioDao.crear(envios.get(j), connection);
                registrosInsertados++;
            }
            
            // Hacer commit después de insertar cada batch
            connection.commit();
            connection.setAutoCommit(false);
            
            // Mostrar progreso
            System.out.print(".");
            if (end % 1000 == 0 || end == total) {
                System.out.println(" " + registrosInsertados + "/" + total);
            }
        }
        
        System.out.println("✓ Total de envíos insertados: " + registrosInsertados);
    }
    
    /**
     * Inserta los pedidos en la base de datos usando batch inserts para mejor rendimiento.
     */
    private void insertarPedidos(List<Pedido> pedidos, Connection connection) throws SQLException {
        int batchSize = 1000;
        int total = pedidos.size();
        int registrosInsertados = 0;
        
        System.out.println("Insertando " + total + " pedidos en la base de datos...");
        
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            
            // Insertar registros del batch actual
            for (int j = i; j < end; j++) {
                pedidoDao.crear(pedidos.get(j), connection);
                registrosInsertados++;
            }
            
            // Hacer commit después de insertar cada batch
            connection.commit();
            connection.setAutoCommit(false);
            
            // Mostrar progreso
            System.out.print(".");
            if (end % 1000 == 0 || end == total) {
                System.out.println(" " + registrosInsertados + "/" + total);
            }
        }
        
        System.out.println("✓ Total de pedidos insertados: " + registrosInsertados);
    }
    
    /**
     * Muestra estadísticas de los datos insertados.
     */
    private void mostrarEstadisticas(Connection connection) throws SQLException {
        // Contar envíos
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM envios WHERE eliminado = FALSE")) {
            if (rs.next()) {
                System.out.println("\nTotal de envíos: " + rs.getInt(1));
            }
        }
        
        // Contar pedidos
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pedidos WHERE eliminado = FALSE")) {
            if (rs.next()) {
                System.out.println("Total de pedidos: " + rs.getInt(1));
            }
        }
        
        // Contar pedidos con envío nulo
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pedidos WHERE envio IS NULL AND eliminado = FALSE")) {
            if (rs.next()) {
                System.out.println("Pedidos con envío nulo: " + rs.getInt(1));
            }
        }
    }
    
    /**
     * Método main para ejecutar el seeder.
     */
    public static void main(String[] args) {
        try {
            DatabaseSeeder seeder = new DatabaseSeeder();
            seeder.seed();
        } catch (SQLException e) {
            System.err.println("Error al ejecutar el seeder: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

