package tfi.main;

import tfi.entities.Pedido;
import tfi.entities.Envio;
import tfi.service.PedidoService;
import tfi.service.EnvioService;
import tfi.service.ServiceException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Clase que maneja el menú de la aplicación y la interacción con el usuario.
 */
public class AppMenu {
    
    private final Scanner scanner;
    private final PedidoService pedidoService;
    private final EnvioService envioService;
    private final DateTimeFormatter dateFormatter;
    
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        this.pedidoService = new PedidoService();
        this.envioService = new EnvioService();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
    
    /**
     * Inicia el menú principal de la aplicación.
     */
    public void iniciar() {
        System.out.println("=== SISTEMA DE GESTIÓN DE PEDIDOS Y ENVÍOS ===");
        System.out.println("Bienvenido al sistema de gestión de pedidos y envíos.");
        
        boolean continuar = true;
        while (continuar) {
            mostrarMenuPrincipal();
            String opcion = leerEntrada("Seleccione una opción: ").toUpperCase();
            
            try {
                switch (opcion) {
                    case "1":
                        gestionarPedidos();
                        break;
                    case "2":
                        gestionarEnvios();
                        break;
                    case "3":
                        asociarEnvioAPedido();
                        break;
                    case "4":
                        crearPedidoConEnvio();
                        break;
                    case "5":
                        crearEnvioYAsociarAPedido();
                        break;
                    case "6":
                        buscarPedidosPorCliente();
                        break;
                    case "7":
                        buscarEnvioPorTracking();
                        break;
                    case "0":
                        continuar = false;
                        System.out.println("¡Gracias por usar el sistema!");
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                }
            } catch (ServiceException e) {
                System.err.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error inesperado: " + e.getMessage());
            }
            
            if (continuar) {
                System.out.println("\nPresione Enter para continuar...");
                scanner.nextLine();
            }
        }
        
        scanner.close();
    }
    
    /**
     * Muestra el menú principal.
     */
    private void mostrarMenuPrincipal() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        System.out.println("1. Gestionar Pedidos");
        System.out.println("2. Gestionar Envíos");
        System.out.println("3. Asociar Envío a Pedido");
        System.out.println("4. Crear Pedido con Envío (Transacción)");
        System.out.println("5. Crear Envío y Asociar a Pedido (Transacción)");
        System.out.println("6. Buscar Pedidos por Cliente");
        System.out.println("7. Buscar Envío por Tracking");
        System.out.println("0. Salir");
        System.out.println("========================");
    }
    
    /**
     * Gestiona las operaciones CRUD de pedidos.
     */
    private void gestionarPedidos() {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n=== GESTIÓN DE PEDIDOS ===");
            System.out.println("1. Crear Pedido");
            System.out.println("2. Buscar Pedido por ID");
            System.out.println("3. Listar Todos los Pedidos");
            System.out.println("4. Actualizar Pedido");
            System.out.println("5. Eliminar Pedido");
            System.out.println("0. Volver al Menú Principal");
            System.out.println("===========================");
            
            String opcion = leerEntrada("Seleccione una opción: ").toUpperCase();
            
            try {
                switch (opcion) {
                    case "1":
                        crearPedido();
                        break;
                    case "2":
                        buscarPedidoPorId();
                        break;
                    case "3":
                        listarPedidos();
                        break;
                    case "4":
                        actualizarPedido();
                        break;
                    case "5":
                        eliminarPedido();
                        break;
                    case "0":
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (ServiceException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gestiona las operaciones CRUD de envíos.
     */
    private void gestionarEnvios() {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n=== GESTIÓN DE ENVÍOS ===");
            System.out.println("1. Crear Envío");
            System.out.println("2. Buscar Envío por ID");
            System.out.println("3. Listar Todos los Envíos");
            System.out.println("4. Actualizar Envío");
            System.out.println("5. Eliminar Envío");
            System.out.println("0. Volver al Menú Principal");
            System.out.println("===========================");
            
            String opcion = leerEntrada("Seleccione una opción: ").toUpperCase();
            
            try {
                switch (opcion) {
                    case "1":
                        crearEnvio();
                        break;
                    case "2":
                        buscarEnvioPorId();
                        break;
                    case "3":
                        listarEnvios();
                        break;
                    case "4":
                        actualizarEnvio();
                        break;
                    case "5":
                        eliminarEnvio();
                        break;
                    case "0":
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (ServiceException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Crea un nuevo pedido.
     */
    private void crearPedido() throws ServiceException {
        System.out.println("\n--- CREAR PEDIDO ---");
        
        String numero = leerEntrada("Número del pedido: ");
        LocalDate fecha = leerFecha("Fecha del pedido (dd/MM/yyyy): ");
        String clienteNombre = leerEntrada("Nombre del cliente: ");
        double total = leerDouble("Total del pedido: ");
        
        Pedido pedido = new Pedido(numero, fecha, clienteNombre, total);
        Pedido pedidoCreado = pedidoService.insertar(pedido);
        
        System.out.println("Pedido creado exitosamente con ID: " + pedidoCreado.getId());
    }
    
    /**
     * Busca un pedido por ID.
     */
    private void buscarPedidoPorId() throws ServiceException {
        System.out.println("\n--- BUSCAR PEDIDO POR ID ---");
        
        Long id = leerLong("ID del pedido: ");
        Pedido pedido = pedidoService.getById(id);
        
        if (pedido != null) {
            System.out.println("Pedido encontrado:");
            System.out.println(pedido);
        } else {
            System.out.println("No se encontró un pedido con ID: " + id);
        }
    }
    
    /**
     * Lista todos los pedidos.
     */
    private void listarPedidos() throws ServiceException {
        System.out.println("\n--- LISTA DE PEDIDOS ---");
        
        List<Pedido> pedidos = pedidoService.getAll();
        
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
        } else {
            System.out.println("Total de pedidos: " + pedidos.size());
            for (Pedido pedido : pedidos) {
                System.out.println(pedido);
            }
        }
    }
    
    /**
     * Actualiza un pedido existente.
     */
    private void actualizarPedido() throws ServiceException {
        System.out.println("\n--- ACTUALIZAR PEDIDO ---");
        
        Long id = leerLong("ID del pedido a actualizar: ");
        Pedido pedido = pedidoService.getById(id);
        
        if (pedido == null) {
            System.out.println("No se encontró un pedido con ID: " + id);
            return;
        }
        
        System.out.println("Pedido actual:");
        System.out.println(pedido);
        System.out.println("\nIngrese los nuevos datos (presione Enter para mantener el valor actual):");
        
        String numero = leerEntradaOpcional("Número del pedido [" + pedido.getNumero() + "]: ");
        if (!numero.isEmpty()) {
            pedido.setNumero(numero);
        }
        
        String fechaStr = leerEntradaOpcional("Fecha del pedido [" + pedido.getFecha().format(dateFormatter) + "]: ");
        if (!fechaStr.isEmpty()) {
            pedido.setFecha(leerFecha("Fecha del pedido (dd/MM/yyyy): "));
        }
        
        String clienteNombre = leerEntradaOpcional("Nombre del cliente [" + pedido.getClienteNombre() + "]: ");
        if (!clienteNombre.isEmpty()) {
            pedido.setClienteNombre(clienteNombre);
        }
        
        String totalStr = leerEntradaOpcional("Total del pedido [" + pedido.getTotal() + "]: ");
        if (!totalStr.isEmpty()) {
            pedido.setTotal(leerDouble("Total del pedido: "));
        }
        
        String estadoStr = leerEntradaOpcional("Estado [" + pedido.getEstado() + "]: ");
        if (!estadoStr.isEmpty()) {
            pedido.setEstado(Pedido.EstadoPedido.valueOf(estadoStr.toUpperCase()));
        }
        
        Pedido pedidoActualizado = pedidoService.actualizar(pedido);
        System.out.println("Pedido actualizado exitosamente:");
        System.out.println(pedidoActualizado);
    }
    
    /**
     * Elimina un pedido.
     */
    private void eliminarPedido() throws ServiceException {
        System.out.println("\n--- ELIMINAR PEDIDO ---");
        
        Long id = leerLong("ID del pedido a eliminar: ");
        Pedido pedido = pedidoService.getById(id);
        
        if (pedido == null) {
            System.out.println("No se encontró un pedido con ID: " + id);
            return;
        }
        
        System.out.println("Pedido a eliminar:");
        System.out.println(pedido);
        
        String confirmacion = leerEntrada("¿Está seguro de que desea eliminar este pedido? (s/n): ");
        if (confirmacion.toLowerCase().equals("s")) {
            boolean eliminado = pedidoService.eliminar(id);
            if (eliminado) {
                System.out.println("Pedido eliminado exitosamente.");
            } else {
                System.out.println("No se pudo eliminar el pedido.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }
    
    /**
     * Crea un nuevo envío.
     */
    private void crearEnvio() throws ServiceException {
        System.out.println("\n--- CREAR ENVÍO ---");
        
        String tracking = leerEntrada("Número de tracking: ");
        Envio.EmpresaEnvio empresa = leerEmpresaEnvio();
        Envio.TipoEnvio tipo = leerTipoEnvio();
        double costo = leerDouble("Costo del envío: ");
        
        Envio envio = new Envio(tracking, empresa, tipo, costo);
        Envio envioCreado = envioService.insertar(envio);
        
        System.out.println("Envío creado exitosamente con ID: " + envioCreado.getId());
    }
    
    /**
     * Busca un envío por ID.
     */
    private void buscarEnvioPorId() throws ServiceException {
        System.out.println("\n--- BUSCAR ENVÍO POR ID ---");
        
        Long id = leerLong("ID del envío: ");
        Envio envio = envioService.getById(id);
        
        if (envio != null) {
            System.out.println("Envío encontrado:");
            System.out.println(envio);
        } else {
            System.out.println("No se encontró un envío con ID: " + id);
        }
    }
    
    /**
     * Lista todos los envíos.
     */
    private void listarEnvios() throws ServiceException {
        System.out.println("\n--- LISTA DE ENVÍOS ---");
        
        List<Envio> envios = envioService.getAll();
        
        if (envios.isEmpty()) {
            System.out.println("No hay envíos registrados.");
        } else {
            System.out.println("Total de envíos: " + envios.size());
            for (Envio envio : envios) {
                System.out.println(envio);
            }
        }
    }
    
    /**
     * Actualiza un envío existente.
     */
    private void actualizarEnvio() throws ServiceException {
        System.out.println("\n--- ACTUALIZAR ENVÍO ---");
        
        Long id = leerLong("ID del envío a actualizar: ");
        Envio envio = envioService.getById(id);
        
        if (envio == null) {
            System.out.println("No se encontró un envío con ID: " + id);
            return;
        }
        
        System.out.println("Envío actual:");
        System.out.println(envio);
        System.out.println("\nIngrese los nuevos datos (presione Enter para mantener el valor actual):");
        
        String tracking = leerEntradaOpcional("Tracking [" + envio.getTracking() + "]: ");
        if (!tracking.isEmpty()) {
            envio.setTracking(tracking);
        }
        
        String empresaStr = leerEntradaOpcional("Empresa [" + envio.getEmpresa() + "]: ");
        if (!empresaStr.isEmpty()) {
            envio.setEmpresa(leerEmpresaEnvio());
        }
        
        String tipoStr = leerEntradaOpcional("Tipo [" + envio.getTipo() + "]: ");
        if (!tipoStr.isEmpty()) {
            envio.setTipo(leerTipoEnvio());
        }
        
        String costoStr = leerEntradaOpcional("Costo [" + envio.getCosto() + "]: ");
        if (!costoStr.isEmpty()) {
            envio.setCosto(leerDouble("Costo del envío: "));
        }
        
        String estadoStr = leerEntradaOpcional("Estado [" + envio.getEstado() + "]: ");
        if (!estadoStr.isEmpty()) {
            envio.setEstado(Envio.EstadoEnvio.valueOf(estadoStr.toUpperCase()));
        }
        
        Envio envioActualizado = envioService.actualizar(envio);
        System.out.println("Envío actualizado exitosamente:");
        System.out.println(envioActualizado);
    }
    
    /**
     * Elimina un envío.
     */
    private void eliminarEnvio() throws ServiceException {
        System.out.println("\n--- ELIMINAR ENVÍO ---");
        
        Long id = leerLong("ID del envío a eliminar: ");
        Envio envio = envioService.getById(id);
        
        if (envio == null) {
            System.out.println("No se encontró un envío con ID: " + id);
            return;
        }
        
        System.out.println("Envío a eliminar:");
        System.out.println(envio);
        
        String confirmacion = leerEntrada("¿Está seguro de que desea eliminar este envío? (s/n): ");
        if (confirmacion.toLowerCase().equals("s")) {
            boolean eliminado = envioService.eliminar(id);
            if (eliminado) {
                System.out.println("Envío eliminado exitosamente.");
            } else {
                System.out.println("No se pudo eliminar el envío.");
            }
        } else {
            System.out.println("Operación cancelada.");
        }
    }
    
    /**
     * Asocia un envío a un pedido.
     */
    private void asociarEnvioAPedido() throws ServiceException {
        System.out.println("\n--- ASOCIAR ENVÍO A PEDIDO ---");
        
        Long pedidoId = leerLong("ID del pedido: ");
        Long envioId = leerLong("ID del envío: ");
        
        Pedido pedidoActualizado = pedidoService.asociarEnvio(pedidoId, envioId);
        System.out.println("Envío asociado exitosamente al pedido:");
        System.out.println(pedidoActualizado);
    }
    
    /**
     * Busca pedidos por nombre de cliente.
     */
    private void buscarPedidosPorCliente() throws ServiceException {
        System.out.println("\n--- BUSCAR PEDIDOS POR CLIENTE ---");
        
        String clienteNombre = leerEntrada("Nombre del cliente: ");
        List<Pedido> pedidos = pedidoService.buscarPorCliente(clienteNombre);
        
        if (pedidos.isEmpty()) {
            System.out.println("No se encontraron pedidos para el cliente: " + clienteNombre);
        } else {
            System.out.println("Pedidos encontrados para el cliente '" + clienteNombre + "':");
            for (Pedido pedido : pedidos) {
                System.out.println(pedido);
            }
        }
    }
    
    /**
     * Busca un envío por número de tracking.
     */
    private void buscarEnvioPorTracking() throws ServiceException {
        System.out.println("\n--- BUSCAR ENVÍO POR TRACKING ---");
        
        String tracking = leerEntrada("Número de tracking: ");
        Envio envio = envioService.buscarPorTracking(tracking);
        
        if (envio != null) {
            System.out.println("Envío encontrado:");
            System.out.println(envio);
        } else {
            System.out.println("No se encontró un envío con tracking: " + tracking);
        }
    }
    
    // Métodos auxiliares para lectura de datos
    
    private String leerEntrada(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }
    
    private String leerEntradaOpcional(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }
    
    private Long leerLong(String mensaje) {
        while (true) {
            try {
                String input = leerEntrada(mensaje);
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un número válido.");
            }
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            try {
                String input = leerEntrada(mensaje);
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un número válido.");
            }
        }
    }
    
    private LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                String input = leerEntrada(mensaje);
                return LocalDate.parse(input, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Por favor, ingrese una fecha válida en formato dd/MM/yyyy.");
            }
        }
    }
    
    private Envio.EmpresaEnvio leerEmpresaEnvio() {
        while (true) {
            System.out.println("Empresas disponibles:");
            System.out.println("1. ANDREANI");
            System.out.println("2. OCA");
            System.out.println("3. CORREO_ARG");
            
            String opcion = leerEntrada("Seleccione una empresa (1-3): ");
            
            switch (opcion) {
                case "1":
                    return Envio.EmpresaEnvio.ANDREANI;
                case "2":
                    return Envio.EmpresaEnvio.OCA;
                case "3":
                    return Envio.EmpresaEnvio.CORREO_ARG;
                default:
                    System.out.println("Opción inválida. Por favor, seleccione 1, 2 o 3.");
            }
        }
    }
    
    private Envio.TipoEnvio leerTipoEnvio() {
        while (true) {
            System.out.println("Tipos de envío disponibles:");
            System.out.println("1. ESTANDAR");
            System.out.println("2. EXPRES");
            
            String opcion = leerEntrada("Seleccione un tipo (1-2): ");
            
            switch (opcion) {
                case "1":
                    return Envio.TipoEnvio.ESTANDAR;
                case "2":
                    return Envio.TipoEnvio.EXPRES;
                default:
                    System.out.println("Opción inválida. Por favor, seleccione 1 o 2.");
            }
        }
    }
    
    /**
     * OPERACIÓN COMPUESTA: Crea un pedido con un envío en una sola transacción.
     */
    private void crearPedidoConEnvio() throws ServiceException {
        System.out.println("\n--- CREAR PEDIDO CON ENVÍO (OPERACIÓN COMPUESTA) ---");
        System.out.println("Esta operación creará un pedido y un envío en una sola transacción.");
        
        // Datos del pedido
        System.out.println("\n--- DATOS DEL PEDIDO ---");
        String numero = leerEntrada("Número del pedido: ");
        LocalDate fecha = leerFecha("Fecha del pedido (dd/MM/yyyy): ");
        String clienteNombre = leerEntrada("Nombre del cliente: ");
        double total = leerDouble("Total del pedido: ");
        
        Pedido pedido = new Pedido(numero, fecha, clienteNombre, total);
        
        // Datos del envío
        System.out.println("\n--- DATOS DEL ENVÍO ---");
        String tracking = leerEntrada("Número de tracking: ");
        Envio.EmpresaEnvio empresa = leerEmpresaEnvio();
        Envio.TipoEnvio tipo = leerTipoEnvio();
        double costo = leerDouble("Costo del envío: ");
        
        Envio envio = new Envio(tracking, empresa, tipo, costo);
        
        // Ejecutar operación compuesta
        Pedido pedidoCreado = pedidoService.crearPedidoConEnvio(pedido, envio);
        
        System.out.println("\n¡Operación compuesta exitosa!");
        System.out.println("Pedido creado con ID: " + pedidoCreado.getId());
        System.out.println("Envío creado con ID: " + pedidoCreado.getEnvio().getId());
        System.out.println("Pedido completo:");
        System.out.println(pedidoCreado);
    }
    
    /**
     * OPERACIÓN COMPUESTA: Crea un envío y lo asocia a un pedido existente.
     */
    private void crearEnvioYAsociarAPedido() throws ServiceException {
        System.out.println("\n--- CREAR ENVÍO Y ASOCIAR A PEDIDO (OPERACIÓN COMPUESTA) ---");
        System.out.println("Esta operación creará un envío y lo asociará a un pedido existente en una sola transacción.");
        
        // ID del pedido
        Long pedidoId = leerLong("ID del pedido existente: ");
        
        // Verificar que el pedido existe
        Pedido pedido = pedidoService.getById(pedidoId);
        if (pedido == null) {
            System.out.println("No se encontró un pedido con ID: " + pedidoId);
            return;
        }
        
        System.out.println("Pedido encontrado:");
        System.out.println(pedido);
        
        if (pedido.getEnvio() != null) {
            System.out.println("Este pedido ya tiene un envío asociado. No se puede crear otro.");
            return;
        }
        
        // Datos del envío
        System.out.println("\n--- DATOS DEL ENVÍO ---");
        String tracking = leerEntrada("Número de tracking: ");
        Envio.EmpresaEnvio empresa = leerEmpresaEnvio();
        Envio.TipoEnvio tipo = leerTipoEnvio();
        double costo = leerDouble("Costo del envío: ");
        
        Envio envio = new Envio(tracking, empresa, tipo, costo);
        
        // Ejecutar operación compuesta
        Pedido pedidoActualizado = pedidoService.crearEnvioYAsociarAPedido(pedidoId, envio);
        
        System.out.println("\n¡Operación compuesta exitosa!");
        System.out.println("Envío creado con ID: " + pedidoActualizado.getEnvio().getId());
        System.out.println("Pedido actualizado:");
        System.out.println(pedidoActualizado);
    }
}
